package ru.otus.hw.coffee.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.coffee.config.CoffeeProperties;
import ru.otus.hw.coffee.domain.Order;
import ru.otus.hw.coffee.domain.Receipt;

@Service
@Slf4j
public class NotificationService {
    private final String defaultChannel;

    public NotificationService(CoffeeProperties props) {
        this.defaultChannel = props.getNotify().getChannel();
    }

    public void notifyReady(Order order) {
        String ch = order.notifyChannel() != null ? order.notifyChannel() : defaultChannel;
        log.info("[NOTIFY:{}] Заказ {} для клиента {}: напиток готов",
                ch, order.orderId(), order.customerId());
    }

    public void printReceipt(Receipt r) {
        log.info("[RECEIPT] #{} order={} amount={} ts={}",
                r.receiptNo(), r.orderId(), r.amountCents(), r.ts());
    }
}