package hu.progmasters.webshop.exception;

public class OrderNotBelongToCustomerException extends RuntimeException {

    public OrderNotBelongToCustomerException(Long customerId) {
        super("Order does not belong to customer with id: " + customerId);
    }
}
