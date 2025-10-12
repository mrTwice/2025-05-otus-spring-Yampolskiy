package ru.otus.hw.coffee.util;


import java.util.UUID;

public final class Ids {

    private Ids() {
    }

    public static String orderId() {
        return "ORD-" + UUID.randomUUID();
    }
}
