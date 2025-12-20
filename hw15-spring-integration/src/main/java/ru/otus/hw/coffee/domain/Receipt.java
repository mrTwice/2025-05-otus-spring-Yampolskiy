package ru.otus.hw.coffee.domain;

public record Receipt(String orderId, String receiptNo, int amountCents, long ts) { }
