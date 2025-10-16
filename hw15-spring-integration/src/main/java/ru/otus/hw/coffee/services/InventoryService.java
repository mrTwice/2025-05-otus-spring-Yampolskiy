package ru.otus.hw.coffee.services;

import org.springframework.stereotype.Service;
import ru.otus.hw.coffee.config.CoffeeProperties;
import ru.otus.hw.coffee.domain.Ingredient;
import ru.otus.hw.coffee.domain.LowStock;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryService {

    private final Map<Ingredient, Integer> stock;

    private final int lowThreshold;

    public InventoryService(CoffeeProperties props) {
        this.stock = new EnumMap<>(Ingredient.class);
        Arrays.stream(Ingredient.values()).forEach(i ->
                stock.put(i, props.getStock().getOrDefault(i, 0))
        );
        this.lowThreshold = props.getLowStockThreshold();
    }

    public boolean hasEnough(Map<Ingredient, Integer> need) {
        return need.entrySet().stream().allMatch(e -> stock.getOrDefault(e.getKey(), 0) >= e.getValue());
    }

    public synchronized Map<Ingredient, Integer> reserve(Map<Ingredient, Integer> need) {
        if (!hasEnough(need)) {
            throw new IllegalStateException("Not enough ingredients");
        }
        need.forEach((k, v) -> stock.put(k, stock.get(k) - v));
        return Map.copyOf(stock);
    }

    public List<LowStock> lowStockEvents() {
        return stock.entrySet().stream()
                .filter(e -> e.getValue() <= lowThreshold)
                .map(e -> new LowStock(e.getKey(), e.getValue()))
                .toList();
    }

    public Map<Ingredient, Integer> snapshot() {
        return Map.copyOf(stock);
    }
}
