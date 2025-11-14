package hu.progmasters.webshop.exception;

public class OrderNotExistsException extends RuntimeException {

    public OrderNotExistsException(Long orderId) {
        super("Order with id: " + orderId + " does not exist.");
    }

}
