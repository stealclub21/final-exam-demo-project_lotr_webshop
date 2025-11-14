package hu.progmasters.webshop.exception;

public class ProductAlreadyExistsException extends RuntimeException{

    public ProductAlreadyExistsException(String name, String vendor) {
        super("Product already exists with name: " + name + " and vendor: " + vendor);
    }
}
