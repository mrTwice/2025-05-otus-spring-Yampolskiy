package ru.otus.hw.coffee.config;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentConfigProperties {

    private int successRatePercent;

    private String provider;

}
