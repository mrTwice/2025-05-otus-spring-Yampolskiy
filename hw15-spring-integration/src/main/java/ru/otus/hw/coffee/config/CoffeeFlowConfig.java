package ru.otus.hw.coffee.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.messaging.MessageHeaders;
import ru.otus.hw.coffee.domain.Beverage;
import ru.otus.hw.coffee.domain.BeveragePrepared;
import ru.otus.hw.coffee.domain.InventoryReserved;
import ru.otus.hw.coffee.domain.Order;
import ru.otus.hw.coffee.domain.OrderAccepted;
import ru.otus.hw.coffee.domain.OrderCompleted;
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

import java.util.Map;
import java.util.UUID;

import static ru.otus.hw.coffee.config.ChannelsConfig.CH_ORDERS_IN;
import static ru.otus.hw.coffee.config.ChannelsConfig.CH_ORDERS_ACCEPTED;
import static ru.otus.hw.coffee.config.ChannelsConfig.CH_TO_BREW;
import static ru.otus.hw.coffee.config.ChannelsConfig.CH_COMPLETED;
import static ru.otus.hw.coffee.config.ChannelsConfig.CH_TECH;
import static ru.otus.hw.coffee.config.ChannelsConfig.CH_ERRORS;


@Configuration
@EnableIntegration
public class CoffeeFlowConfig {

    public static final String CH_ACCEPTED_ENRICHED = "orders.accepted.enriched";

    public static final String CH_RESERVED = "orders.accepted.reserved";

    public static final String CH_PAID = "orders.accepted.paid";

    public static final String CH_BREW_INPUT = "brew.subflow.input";

    public static final String CH_ORDER_WITH_RECIPE = "brew.orderWithRecipe";

    public static final String CH_BREW_DONE = "brew.done";

    public static final String CH_NOTIFY_DONE = "brew.notifyDone";

    @Bean
    public IntegrationFlow orderFlow() {
        return IntegrationFlow.from(CH_ORDERS_IN)
                .transform(Order.class, o -> new Order(
                        o.orderId() != null ? o.orderId() : Ids.orderId(),
                        o.customerId(), o.beverage(),
                        Math.max(0, o.sugarSpoons()), o.notifyChannel()))
                .wireTap(CH_TECH)
                .routeToRecipients(r -> r
                        .recipient(CH_ORDERS_ACCEPTED))
                .get();
    }

    @Bean
    public IntegrationFlow acceptedToEnrichedFlow(Map<Beverage, Recipe> recipes) {
        return IntegrationFlow.from(CH_ORDERS_ACCEPTED)
                .transform(Order.class, OrderAccepted::new)
                .enrichHeaders(h -> h.headerFunction(Headers.PRICE, m ->
                        recipes.get(((OrderAccepted) m.getPayload()).order().beverage()).priceCents()))
                .channel(CH_ACCEPTED_ENRICHED)
                .get();
    }

    @Bean
    public IntegrationFlow enrichedToReservedFlow(InventoryService inv, Map<Beverage, Recipe> recipes) {
        return IntegrationFlow.from(CH_ACCEPTED_ENRICHED)
                .handle(OrderAccepted.class, (p, h) -> {
                    var o = p.order();
                    var r = recipes.get(o.beverage());
                    inv.reserve(r.grams());
                    return new InventoryReserved(o);
                })
                .channel(CH_RESERVED)
                .get();
    }

    @Bean
    public IntegrationFlow reservedToPaidFlow(PaymentService pay) {
        return IntegrationFlow.from(CH_RESERVED)
                .handle(InventoryReserved.class, (e, h) -> {
                    int amount = (Integer) h.get(Headers.PRICE);
                    var payment = pay.charge(e.order().orderId(), amount, "CARD");
                    return new PaymentApproved(e.order(), payment);
                })
                .filter((PaymentApproved p) -> p.payment().approved(), f -> f.discardChannel(CH_ERRORS))
                .channel(CH_PAID)
                .get();
    }

    @Bean
    public IntegrationFlow paidToBrewFlow() {
        return IntegrationFlow.from(CH_PAID)
                .enrichHeaders(h -> h
                        .headerFunction(Headers.AMOUNT, m -> ((PaymentApproved) m.getPayload()).payment().amountCents())
                        .headerFunction(Headers.TXN_ID, m -> ((PaymentApproved) m.getPayload()).payment().txnId())
                        .header(Headers.METHOD, "CARD")
                        .headerFunction(Headers.CORR, m -> ((PaymentApproved) m.getPayload()).order().orderId()))
                .channel(CH_TO_BREW)
                .get();
    }


    @Bean
    public IntegrationFlow toBrewFlow() {
        return IntegrationFlow.from(CH_TO_BREW)
                .gateway("brew.subflow.input")
                .get();
    }

    @Bean(name = "brew.subflow.prepare")
    public IntegrationFlow brewPrepare(Map<Beverage, Recipe> recipes) {
        return IntegrationFlow.from(CH_BREW_INPUT)
                .transform(PaymentApproved.class, PaymentApproved::order)
                .transform(Order.class, o -> new OrderAndRecipe(o, recipes.get(o.beverage())))
                .channel(CH_ORDER_WITH_RECIPE)
                .get();
    }

    @Bean(name = "brew.subflow.brew")
    public IntegrationFlow brewProcess(BaristaService barista) {
        return IntegrationFlow.from(CH_ORDER_WITH_RECIPE)
                .handle((OrderAndRecipe p, MessageHeaders h) -> {
                    barista.brew(p.recipe(), p.order());
                    return p;
                })
                .transform(OrderAndRecipe.class, p -> new BeveragePrepared(p.order()))
                .channel(CH_BREW_DONE)
                .get();
    }

    @Bean(name = "brew.subflow.notify")
    public IntegrationFlow brewNotify(NotificationService notify) {
        return IntegrationFlow.from(CH_BREW_DONE)
                .handle(BeveragePrepared.class, (bp, h) -> {
                    notify.notifyReady(bp.order());
                    return bp;
                })
                .channel(CH_NOTIFY_DONE)
                .get();
    }

    @Bean(name = "brew.subflow.receipt")
    public IntegrationFlow brewReceipt(NotificationService notify) {
        return IntegrationFlow.from(CH_NOTIFY_DONE)
                .handle(BeveragePrepared.class, (bp, h) -> {
                    var o = bp.order();
                    int amount = (Integer) h.getOrDefault(Headers.AMOUNT, 0);
                    var receipt = new Receipt(o.orderId(), "R-" + UUID.randomUUID(), amount, System.currentTimeMillis());
                    notify.printReceipt(receipt);
                    return new ReceiptPrinted(o, receipt);
                })
                .channel(CH_COMPLETED)
                .get();
    }


    @Bean
    public IntegrationFlow completedFlow() {
        return IntegrationFlow.from(CH_COMPLETED)
                .transform(ReceiptPrinted.class, rp -> new OrderCompleted(rp.order()))
                .log(LoggingHandler.Level.INFO, "ORDER",
                        (m) -> "[ORDER DONE] %s txn=%s method=%s".formatted(
                                m.getPayload(),
                                m.getHeaders().get(Headers.TXN_ID),
                                m.getHeaders().get(Headers.METHOD)))
                .get();
    }

    @Bean
    public IntegrationFlow routeGlobalErrors() {
        return IntegrationFlow.from("errors")
                .channel(ChannelsConfig.CH_ERRORS)
                .get();
    }
}
