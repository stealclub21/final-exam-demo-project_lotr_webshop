package hu.progmasters.webshop.exception;

public class ProductNotExistsException extends RuntimeException{

    public ProductNotExistsException(Long productId) {
        super("Product with id: " + productId + " does not exist.");
    }
}
