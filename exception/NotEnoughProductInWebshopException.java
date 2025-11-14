package hu.progmasters.webshop.exception;

public class NotEnoughProductInWebshopException extends RuntimeException {

    public NotEnoughProductInWebshopException(Long productId) {
        super("Not enough product in webshop with id: " + productId);
    }
}
