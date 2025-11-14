package hu.progmasters.webshop.exception;

public class NotEnoughProductInBombadilsEmporiumException extends RuntimeException {

    public NotEnoughProductInBombadilsEmporiumException(Long id) {
        super("Not enough product in Bombadil's Emporium with product id: " + id);
    }
}
