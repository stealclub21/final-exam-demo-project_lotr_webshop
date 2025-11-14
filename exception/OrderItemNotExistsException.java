package hu.progmasters.webshop.exception;

public class OrderItemNotExistsException extends RuntimeException {

    public OrderItemNotExistsException(Long itemId) {
        super("Order item not found with id: " + itemId);
    }
}
