package ru.otus.hw.coffee.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import ru.otus.hw.coffee.domain.Ingredient;

import java.util.EnumMap;
import java.util.Map;


@Setter
@Getter
@ConfigurationProperties(prefix = "coffee")
public class CoffeeProperties {

    private Map<Ingredient, Integer> stock = new EnumMap<>(Ingredient.class);

    private int lowStockThreshold = 200;

    @NestedConfigurationProperty
    private PaymentConfigProperties payment = new PaymentConfigProperties();

    @NestedConfigurationProperty
    private NotifyConfigProperties notify = new NotifyConfigProperties();

    private Map<String, RecipeConfigProperties> recipes;

    private Map<String, Integer> prices;

}
