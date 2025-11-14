package hu.progmasters.webshop.exception;

public class InvalidAddressTypeOrCityException extends RuntimeException{

    public InvalidAddressTypeOrCityException() {
        super("Invalid address type or city!");
    }
}
