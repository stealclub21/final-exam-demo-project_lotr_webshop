package hu.progmasters.webshop.exception;

public class ProductCategoryInUseException extends RuntimeException {

    public ProductCategoryInUseException(Long productCategoryId) {
        super("Product category with id " + productCategoryId + " is in use, cannot be deleted!");
    }
}
