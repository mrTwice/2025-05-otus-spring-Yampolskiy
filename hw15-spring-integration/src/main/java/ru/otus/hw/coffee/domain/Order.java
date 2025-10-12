package ru.otus.hw.coffee.domain;

public record Order(
        String orderId,
        String customerId,
        Beverage beverage,
        int sugarSpoons,
        String notifyChannel
) { }
