package hu.progmasters.webshop.exception;

public class ProductNotFoundInBombadilsEmporiumException extends RuntimeException {

    public ProductNotFoundInBombadilsEmporiumException(Long productId) {
        super("Product not found in Bombadil's Emporium with id: " + productId);
    }
}
