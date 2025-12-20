package ru.otus.hw.coffee.services;

import org.springframework.stereotype.Service;
import ru.otus.hw.coffee.config.CoffeeProperties;
import ru.otus.hw.coffee.domain.Payment;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentService {
    private final int successRate;

    public PaymentService(CoffeeProperties props) {
        this.successRate = props.getPayment().getSuccessRatePercent();
    }

    public Payment charge(String orderId, int amountCents, String method) {
        boolean ok = ThreadLocalRandom.current().nextInt(100) < successRate;
        return new Payment(orderId, amountCents, method, ok, ok ? "TXN-" + UUID.randomUUID() : null);
    }
}