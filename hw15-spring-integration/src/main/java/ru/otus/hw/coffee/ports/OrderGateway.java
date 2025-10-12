package ru.otus.hw.coffee.ports;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.Message;
import ru.otus.hw.coffee.domain.Order;

@MessagingGateway
public interface OrderGateway {
    @Gateway(requestChannel = "orders.input")
    Message<Order> placeOrder(Order order);
}