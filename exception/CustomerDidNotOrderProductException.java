package hu.progmasters.webshop.exception;

public class CustomerDidNotOrderProductException extends RuntimeException {

    public CustomerDidNotOrderProductException(Long customerId, Long productId) {
        super("Sorry. You cannot rate this item. Customer with id: " + customerId + " did not order product with id: " + productId);
    }
}
