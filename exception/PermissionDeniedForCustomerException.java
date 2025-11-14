package hu.progmasters.webshop.exception;

public class PermissionDeniedForCustomerException extends RuntimeException {

    public PermissionDeniedForCustomerException(Long id) {
        super("Permission denied for customer with id: " + id);
    }
}
