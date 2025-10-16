package ru.otus.hw.coffee.domain;

import java.util.Map;

public record Recipe(Beverage beverage, Map<Ingredient, Integer> grams, int secondsToBrew, int priceCents) { }
