package hu.progmasters.webshop.exception;

public class TokenNotFoundException extends RuntimeException{

    public TokenNotFoundException() {
        super("Token not found");
    }
}
