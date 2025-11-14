package hu.progmasters.webshop.exception;

public class OrderNotFoundByIdException extends RuntimeException {

    public OrderNotFoundByIdException(Long orderId) {
        super("Order not found by id: " + orderId);
    }
}
