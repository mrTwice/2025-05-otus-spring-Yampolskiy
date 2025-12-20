package ru.otus.hw.coffee.config;


import lombok.Getter;
import lombok.Setter;
import ru.otus.hw.coffee.domain.Ingredient;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class RecipeConfigProperties {

    private int seconds;

    private Map<Ingredient, Integer> grams = new HashMap<>();

}
