package hu.progmasters.webshop.exception;

public class TokenExpiredException extends RuntimeException{

    public TokenExpiredException() {
        super("Token expired!");
    }
}
