package ru.otus.hw.coffee.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.handler.LoggingHandler;
import ru.otus.hw.coffee.services.InventoryService;

import static ru.otus.hw.coffee.config.ChannelsConfig.CH_ERRORS;
import static ru.otus.hw.coffee.config.ChannelsConfig.CH_TECH;
import static ru.otus.hw.coffee.config.ChannelsConfig.CH_LOW_STOCK;

@Configuration
public class MonitoringConfig {
    @Bean
    public IntegrationFlow techWiretap() {
        return f -> f.channel(CH_TECH)
                .log(LoggingHandler.Level.INFO, "TECH", m -> "[TECH] " + m.getPayload());
    }

    @Bean(name = "globalErrorHandler")
    public IntegrationFlow errorsFlow() {
        return IntegrationFlow.from(CH_ERRORS)
                .log(LoggingHandler.Level.ERROR, "ERROR", m -> "[ERROR] " + m.getPayload())
                .get();
    }

    @Bean(name = "stock.monitor")
    public IntegrationFlow stockMonitor(InventoryService inv) {
        return IntegrationFlow.fromSupplier(inv::lowStockEvents, c -> c.poller(Pollers.fixedDelay(3000)))
                .split()
                .channel(CH_LOW_STOCK)
                .get();
    }

    @Bean
    public IntegrationFlow lowStockFlow() {
        return IntegrationFlow.from(CH_LOW_STOCK)
                .log(LoggingHandler.Level.WARN, "LOW_STOCK", m -> "[LOW STOCK] " + m.getPayload())
                .get();
    }
}
