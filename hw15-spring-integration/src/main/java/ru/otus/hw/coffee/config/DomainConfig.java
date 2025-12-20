package ru.otus.hw.coffee.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.otus.hw.coffee.domain.Ingredient;
import ru.otus.hw.coffee.domain.Beverage;
import ru.otus.hw.coffee.domain.Recipe;

import java.util.EnumMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(CoffeeProperties.class)
public class DomainConfig {
    @Bean
    public Map<Beverage, Recipe> recipeBook(CoffeeProperties props) {
        var map = new EnumMap<Beverage, Recipe>(Beverage.class);
        props.getRecipes().forEach((k, v) -> {
            var b = Beverage.valueOf(k);
            var grams = new EnumMap<Ingredient, Integer>(v.getGrams());
            int price = props.getPrices().getOrDefault(k, 0);
            map.put(b, new Recipe(b, grams, v.getSeconds(), price));
        });
        return map;
    }
}
