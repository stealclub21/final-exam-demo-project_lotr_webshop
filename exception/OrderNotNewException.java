package hu.progmasters.webshop.exception;

public class OrderNotNewException extends RuntimeException {

    public OrderNotNewException(Long orderId) {
        super("Order NOT in NEW status, cannot be cancelled with id: " + orderId);
    }
}
