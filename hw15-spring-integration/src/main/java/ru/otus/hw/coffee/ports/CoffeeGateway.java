package ru.otus.hw.coffee.ports;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import ru.otus.hw.coffee.config.ChannelsConfig;
import ru.otus.hw.coffee.domain.Order;

@MessagingGateway
public interface CoffeeGateway {
    @Gateway(requestChannel = ChannelsConfig.CH_ORDERS_IN)
    void placeOrder(Order order);
}