package hu.progmasters.webshop.exception;

import hu.progmasters.webshop.domain.enumeration.CustomerType;

public class CustomerTypeMismatchException extends RuntimeException{

    public CustomerTypeMismatchException(CustomerType customersType, Long customerId, CustomerType productsCustomerType, Long productId) {
        super("Customer (with id: " + customerId + ") is not allowed to order product with id: " + productId +
                ". Customer's type: " + customersType + ", Product's customer type: " + productsCustomerType);
    }
}
