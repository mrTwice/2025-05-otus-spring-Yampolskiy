package ru.otus.hw.coffee.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.otus.hw.coffee.domain.Order;
import ru.otus.hw.coffee.domain.Recipe;

@Service
public class BaristaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaristaService.class);

    public void brew(Recipe recipe, Order order) {
        try {
            Thread.sleep(recipe.secondsToBrew() * 100L);
        } catch (InterruptedException ignored) {
            LOGGER.info("Barista service has been interrupted");
        }
    }
}