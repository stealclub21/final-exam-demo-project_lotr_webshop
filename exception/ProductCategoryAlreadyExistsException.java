package hu.progmasters.webshop.exception;

public class ProductCategoryAlreadyExistsException extends RuntimeException{

    public ProductCategoryAlreadyExistsException(String name) {
        super("Product category with name: " + name + " already exists!");
    }
}
