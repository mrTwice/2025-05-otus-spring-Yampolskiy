package ru.otus.hw.coffee.config;

import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.MessageChannel;

@Configuration
public class ChannelsConfig {

    public static final String CH_ORDERS_IN = "orders.input";

    public static final String CH_ORDERS_ACCEPTED = "orders.accepted";

    public static final String CH_PAYMENTS = "payments";

    public static final String CH_TO_BREW = "orders.toBrew";

    public static final String CH_COMPLETED = "orders.completed";

    public static final String CH_TECH = "tech.log";

    public static final String CH_LOW_STOCK = "inventory.low";

    public static final String CH_ERRORS = "errors";


    @Bean(name = CH_ORDERS_IN)
    public MessageChannel ordersInput() {
        return new DirectChannel();
    }

    @Bean(name = CH_ORDERS_ACCEPTED)
    public MessageChannel accepted() {
        return new DirectChannel();
    }

    @Bean(name = CH_PAYMENTS)
    public MessageChannel payments() {
        return new DirectChannel();
    }

    @Bean(name = CH_TO_BREW)
    public MessageChannel toBrew() {
        return new QueueChannel(100);
    }

    @Bean(name = CH_COMPLETED)
    public MessageChannel completed() {
        return new PublishSubscribeChannel();
    }

    @Bean(name = CH_TECH)
    public MessageChannel tech() {
        return new PublishSubscribeChannel();
    }

    @Bean(name = CH_LOW_STOCK)
    public MessageChannel low() {
        return new PublishSubscribeChannel();
    }

    @Bean(name = CH_ERRORS)
    public MessageChannel errors() {
        return new PublishSubscribeChannel();
    }
}
