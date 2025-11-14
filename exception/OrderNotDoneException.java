package hu.progmasters.webshop.exception;

public class OrderNotDoneException extends RuntimeException{

    public OrderNotDoneException(Long orderId) {
        super("Order not done with id: " + orderId);
    }
}
