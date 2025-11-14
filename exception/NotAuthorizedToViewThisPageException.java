package hu.progmasters.webshop.exception;

public class NotAuthorizedToViewThisPageException extends RuntimeException {

    public NotAuthorizedToViewThisPageException() {
        super("You are not authorized to view this page.");
    }
}
