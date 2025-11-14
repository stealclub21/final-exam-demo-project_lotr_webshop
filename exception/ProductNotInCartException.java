package hu.progmasters.webshop.exception;

public class ProductNotInCartException extends RuntimeException {

    public ProductNotInCartException(Long id) {
        super("Product is not in the cart with id: " + id);
    }
}
