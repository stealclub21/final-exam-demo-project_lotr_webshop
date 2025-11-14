package hu.progmasters.webshop.exception;

public class ProductCategoryNotExistsException extends RuntimeException{

    public ProductCategoryNotExistsException(Long productId) {
        super("Product category with id: " + productId + " does not exist!");
    }
}
