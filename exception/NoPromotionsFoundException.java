package hu.progmasters.webshop.exception;

public class NoPromotionsFoundException extends RuntimeException {

    public NoPromotionsFoundException() {
        super("No promotions found.");
    }
}
