package hu.progmasters.webshop.domain.enumeration;

import lombok.Getter;

@Getter
public enum ShippingMethod {
    PERSONAL_PICKUP(0.0),
    SHIRE_SHIPPING(1.2),
    EAGLE_EXPRESS(3.4),
    NAZGUL_HAULERS(3.4);

    private final double cost;

    ShippingMethod(double cost) {
        this.cost = cost;
    }
}
