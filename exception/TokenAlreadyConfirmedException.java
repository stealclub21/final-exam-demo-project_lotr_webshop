package hu.progmasters.webshop.exception;

public class TokenAlreadyConfirmedException extends RuntimeException{

    public TokenAlreadyConfirmedException() {
        super("Token already confirmed");
    }
}
