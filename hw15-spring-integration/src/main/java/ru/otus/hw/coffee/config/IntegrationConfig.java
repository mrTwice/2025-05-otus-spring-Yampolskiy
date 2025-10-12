package ru.otus.hw.coffee.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Pollers;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import ru.otus.hw.coffee.domain.Ingredient;
import ru.otus.hw.coffee.domain.Beverage;
import ru.otus.hw.coffee.domain.BeveragePrepared;
import ru.otus.hw.coffee.domain.InventoryReserved;
import ru.otus.hw.coffee.domain.Order;
import ru.otus.hw.coffee.domain.OrderAccepted;
import ru.otus.hw.coffee.domain.OrderCompleted;
import ru.otus.hw.coffee.domain.Payment;
import ru.otus.hw.coffee.domain.PaymentApproved;
import ru.otus.hw.coffee.domain.Recipe;
import ru.otus.hw.coffee.domain.ReceiptPrinted;
import ru.otus.hw.coffee.domain.Receipt;
import ru.otus.hw.coffee.domain.OrderAndRecipe;
import ru.otus.hw.coffee.services.BaristaService;
import ru.otus.hw.coffee.services.InventoryService;
import ru.otus.hw.coffee.services.NotificationService;
import ru.otus.hw.coffee.services.PaymentService;
import ru.otus.hw.coffee.util.Ids;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@EnableIntegration
@Configuration
public class IntegrationConfig {

    @Bean(name = "orders.input")
    public MessageChannel ordersInput() {
        return new DirectChannel();
    }

    @Bean(name = "orders.accepted")
    public MessageChannel acceptedOrders() {
        return new DirectChannel();
    }

    @Bean(name = "payments")
    public MessageChannel payments() {
        return new DirectChannel();
    }

    @Bean(name = "orders.toBrew")
    public MessageChannel toBrew() {
        return new QueueChannel(100);
    }

    @Bean(name = "orders.completed")
    public MessageChannel completed() {
        return new PublishSubscribeChannel();
    }

    @Bean(name = "tech.log")
    public MessageChannel techLog() {
        return new PublishSubscribeChannel();
    }

    @Bean(name = "inventory.low")
    public MessageChannel lowStockTopic() {
        return new PublishSubscribeChannel();
    }

    @Bean(name = "errors")
    public MessageChannel errors() {
        return new PublishSubscribeChannel();
    }

    @Bean
    public IntegrationFlow techWiretap() {
        return f -> f
                .channel("tech.log")
                .handle(m -> System.out.println("[TECH] " + m.getPayload()));
    }

    @Bean
    public Map<Beverage, Recipe> recipeBook(CoffeeProperties props) {
        Map<Beverage, Recipe> map = new EnumMap<>(Beverage.class);
        props.getRecipes().forEach((k, v) -> {
            Beverage b = Beverage.valueOf(k);
            Map<Ingredient, Integer> grams = new EnumMap<>(Ingredient.class);
            grams.putAll(v.getGrams());
            int price = props.getPrices().getOrDefault(k, 0);
            map.put(b, new Recipe(b, grams, v.getSeconds(), price));
        });
        return map;
    }

    @Bean
    public IntegrationFlow orderFlow() {
        return IntegrationFlow.from("orders.input")
                .wireTap("tech.log")
                .transform(Order.class, o -> new Order(
                        o.orderId() != null ? o.orderId() : Ids.orderId(),
                        o.customerId(), o.beverage(),
                        Math.max(0, o.sugarSpoons()), o.notifyChannel()
                ))
                .routeToRecipients(r -> r
                        .recipient("orders.accepted")
                        .recipient("tech.log", m -> true))
                .get();
    }



    @Bean(name = "brew.subflow")
    public IntegrationFlow brewSubflow(
            BaristaService barista,
            NotificationService notify,
            Map<Beverage, Recipe> recipes
    ) {
        return IntegrationFlow.from("brew.subflow.input")
                .transform(PaymentApproved.class, PaymentApproved::order)
                .transform(Order.class, o -> new OrderAndRecipe(o, recipes.get(o.beverage())))
                .handle(OrderAndRecipe.class, (p, h) -> {
                    barista.brew(p.recipe(), p.order());
                    return p;
                })
                .transform(OrderAndRecipe.class, p -> new BeveragePrepared(p.order()))
                .handle(BeveragePrepared.class, (bp, h) -> {
                    notify.notifyReady(bp.order());
                    return bp;
                })
                .handle(BeveragePrepared.class, (bp, h) -> {
                    Order o = bp.order();
                    int amount = (Integer) h.getOrDefault("amountCents", 0);
                    Receipt r = new Receipt(
                            o.orderId(),
                            "R-" + java.util.UUID.randomUUID(),
                            amount, System.currentTimeMillis());
                    notify.printReceipt(r);
                    return new ReceiptPrinted(o, r);
                })
                .publishSubscribeChannel(s -> s
                        .subscribe(sf -> sf.handle(m -> { /* audit */ }))
                        .subscribe(sf -> sf.channel("orders.completed"))
                )
                .get();
    }

    @Bean
    public IntegrationFlow acceptedFlow(
            InventoryService inv,
            PaymentService pay,
            Map<Beverage, Recipe> recipes
    ) {
        return IntegrationFlow.from("orders.accepted")
                .handle(Order.class, (o, h) -> new OrderAccepted(o), e -> e.id("wrapOrderAccepted"))
                .enrichHeaders(h -> h.headerFunction("price",
                        m -> recipes.get(((OrderAccepted) m.getPayload()).order().beverage()).priceCents()))
                .<OrderAccepted>handle((p, h) -> {
                    Order o = p.order();
                    Recipe r = recipes.get(o.beverage());
                    inv.reserve(r.grams());
                    return new InventoryReserved(o);
                }).handle((InventoryReserved e, MessageHeaders h) -> {
                    int amount = (Integer) h.get("price");
                    Payment p = pay.charge(e.order().orderId(), amount, "CARD");
                    return new PaymentApproved(e.order(), p);
                })
                .filter((PaymentApproved p) -> p.payment().approved(), f -> f.discardChannel("errors"))
                .enrichHeaders(h -> h
                        .headerFunction("amountCents", m -> ((PaymentApproved) m.getPayload()).payment().amountCents())
                        .headerFunction("txnId", m -> ((PaymentApproved) m.getPayload()).payment().txnId())
                        .header("paymentMethod", "CARD")
                        .headerFunction("correlationId", m -> ((PaymentApproved) m.getPayload()).order().orderId())
                ).routeToRecipients(r -> r
                        .recipient("orders.toBrew")
                        .recipient("tech.log")).get();
    }


    @Bean
    public IntegrationFlow toBrewFlow() {
        return IntegrationFlow.from("orders.toBrew")
                .gateway("brew.subflow.input")
                .get();
    }

    @Bean(name = "brew.adapter")
    public IntegrationFlow brewSubflowAdapter(
            BaristaService barista,
            NotificationService notify,
            Map<Beverage, Recipe> recipes
    ) {
        return IntegrationFlow.from(MessageChannels.direct())
                .transform(PaymentApproved.class, pa -> pa.order())
                .gateway(brewSubflow(barista, notify, recipes))
                .transform(BeveragePrepared.class, bp -> {
                    Order o = bp.order();
                    int amount = 0;
                    return new Receipt(o.orderId(), "R-" + UUID.randomUUID(), amount, System.currentTimeMillis());
                })
                .handle(Receipt.class, (r, h) -> {
                    notify.printReceipt(r);
                    return new ReceiptPrinted(null, r);
                })
                .publishSubscribeChannel(s -> s
                        .subscribe(sf -> sf.handle(m -> {
                        }))
                        .subscribe(sf -> sf.channel("orders.completed")))
                .get();
    }

    @Bean
    public IntegrationFlow completedFlow() {
        return IntegrationFlow.from("orders.completed")
                .transform(ReceiptPrinted.class, rp -> new OrderCompleted(rp.order()))
                .handle(OrderCompleted.class, (oc, headers) -> {
                    System.out.printf("[ORDER DONE] %s txn=%s method=%s%n",
                            oc, headers.get("txnId"), headers.get("paymentMethod"));
                    return null;
                })
                .get();
    }

    @Bean
    public IntegrationFlow stockMonitor(InventoryService inv) {
        return IntegrationFlow
                .fromSupplier(inv::lowStockEvents, c -> c.poller(Pollers.fixedDelay(3000)))
                .split()
                .channel("inventory.low")
                .get();
    }

    @Bean
    public IntegrationFlow lowStockFlow() {
        return IntegrationFlow.from("inventory.low")
                .handle(m -> System.out.println("[LOW STOCK] " + m.getPayload()))
                .get();
    }

    @Bean
    public IntegrationFlow errorsFlow() {
        return IntegrationFlow.from("errors")
                .handle(m -> System.err.println("[ERROR] " + m.getPayload()))
                .get();
    }
}
