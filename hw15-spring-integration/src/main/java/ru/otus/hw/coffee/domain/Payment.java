package ru.otus.hw.coffee.domain;

public record Payment(String orderId, int amountCents, String method, boolean approved, String txnId) { }
