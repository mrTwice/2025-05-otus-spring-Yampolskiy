package ru.otus.hw.coffee;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.otus.hw.coffee.config.CoffeeProperties;
import ru.otus.hw.coffee.domain.Beverage;
import ru.otus.hw.coffee.domain.Order;
import ru.otus.hw.coffee.ports.CoffeeGateway;

@SpringBootApplication
@EnableConfigurationProperties(CoffeeProperties.class)
public class CoffeeApplication implements CommandLineRunner {

    private final CoffeeGateway gateway;

    public CoffeeApplication(CoffeeGateway gateway) {
        this.gateway = gateway;
    }

    public static void main(String[] args) {
        SpringApplication.run(CoffeeApplication.class, args);
    }

    @Override
    public void run(String... args) {
        gateway.placeOrder(new Order(null, "cust-1", Beverage.ESPRESSO, 1, "stdout"));
        gateway.placeOrder(new Order(null, "cust-2", Beverage.CAPPUCCINO, 0, "stdout"));
    }
}