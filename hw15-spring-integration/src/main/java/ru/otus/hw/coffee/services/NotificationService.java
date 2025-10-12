package ru.otus.hw.coffee.services;

import org.springframework.stereotype.Service;
import ru.otus.hw.coffee.config.CoffeeProperties;
import ru.otus.hw.coffee.domain.Order;
import ru.otus.hw.coffee.domain.Receipt;

@Service
public class NotificationService {
    private final String defaultChannel;

    public NotificationService(CoffeeProperties props) {
        this.defaultChannel = props.getNotify().getChannel();
    }

    public void notifyReady(Order order) {
        String ch = order.notifyChannel() != null ? order.notifyChannel() : defaultChannel;
        System.out.printf(
                "[NOTIFY:%s] Заказ %s для клиента %s: напиток готов%n",
                ch,
                order.orderId(),
                order.customerId());
    }

    public void printReceipt(Receipt r) {
        System.out.printf(
                "[RECEIPT] #%s order=%s amount=%d ts=%d%n",
                r.receiptNo(),
                r.orderId(),
                r.amountCents(),
                r.ts());
    }
}