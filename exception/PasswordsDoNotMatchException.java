package hu.progmasters.webshop.exception;

public class PasswordsDoNotMatchException extends RuntimeException{

    public PasswordsDoNotMatchException() {
        super("Passwords do not match.");
    }
}
