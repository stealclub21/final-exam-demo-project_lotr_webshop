package hu.progmasters.webshop.exception;

public class NotEnoughProductInStockException extends RuntimeException {

    public NotEnoughProductInStockException(Long id) {
        super("Not enough product in stock with id: " + id);
    }
}
