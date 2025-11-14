package hu.progmasters.webshop.exception;

import hu.progmasters.webshop.exception.error.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Collections;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(BAD_REQUEST)
    public List<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        List<ErrorResponse> validationErrors = exception.getBindingResult().getFieldErrors().stream()
                                                        .map(fieldError -> new ErrorResponse(fieldError.getField(), fieldError.getDefaultMessage()))
                                                        .toList();
        log.error(exception.getMessage());
        return validationErrors;
    }

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleCustomerAlreadyExistsException(CustomerAlreadyExistsException ex) {
        return Collections.singletonList(new ErrorResponse("email", ex.getMessage()));
    }

    @ExceptionHandler(MissingCustomerTypeException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleMissingCustomerTypeException(MissingCustomerTypeException ex) {
        return Collections.singletonList(new ErrorResponse("customerType", ex.getMessage()));
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleCustomerNotFoundException(CustomerNotFoundException ex) {
        return Collections.singletonList(new ErrorResponse("customerId", ex.getMessage()));
    }

    @ExceptionHandler(CustomerCannotBeReactivatedException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleCustomerCannotBeReactivatedException(CustomerCannotBeReactivatedException ex) {
        return Collections.singletonList(new ErrorResponse("email", ex.getMessage()));
    }

    @ExceptionHandler(ProductAlreadyExistsException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleProductAlreadyExistsException(ProductAlreadyExistsException ex) {
        return Collections.singletonList(new ErrorResponse("name-vendor", ex.getMessage()));
    }

    @ExceptionHandler(ProductCategoryAlreadyExistsException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleProductCategoryAlreadyExistsException(ProductCategoryAlreadyExistsException ex) {
        return Collections.singletonList(new ErrorResponse("name", ex.getMessage()));
    }

    @ExceptionHandler(ProductCategoryNotExistsException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleProductCategoryNotExistsException(ProductCategoryNotExistsException ex) {
        return Collections.singletonList(new ErrorResponse("productId", ex.getMessage()));
    }

    @ExceptionHandler(ProductNotExistsException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleProductNotExistsException(ProductNotExistsException ex) {
        return Collections.singletonList(new ErrorResponse("productId", ex.getMessage()));
    }

    @ExceptionHandler(CustomerAlreadyLinkedToAddressException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleCustomerAlreadyLinkedToAddressException(CustomerAlreadyLinkedToAddressException ex) {
        return Collections.singletonList(new ErrorResponse("customerId", ex.getMessage()));
    }

    @ExceptionHandler(AddressNotExistsException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleAddressNotExistsException(AddressNotExistsException ex) {
        return Collections.singletonList(new ErrorResponse("addressId", ex.getMessage()));
    }

    @ExceptionHandler(AddressDoesNotBelongToCustomerException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleAddressDoesNotBelongToCustomerException(AddressDoesNotBelongToCustomerException ex) {
        return Collections.singletonList(new ErrorResponse("addressId", ex.getMessage()));
    }

    @ExceptionHandler(CustomerDidNotOrderProductException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleCustomerDidNotOrderProductException(CustomerDidNotOrderProductException ex) {
        return Collections.singletonList(new ErrorResponse("productId", ex.getMessage()));
    }

    @ExceptionHandler(InvalidAddressTypeOrCityException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleInvalidAddressTypeOrCityException(InvalidAddressTypeOrCityException ex) {
        return Collections.singletonList(new ErrorResponse("message", ex.getMessage()));
    }

    @ExceptionHandler(CustomerCannotAccessPremiumServicesException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleCustomerNotPremiumException(CustomerCannotAccessPremiumServicesException ex) {
        return Collections.singletonList(new ErrorResponse("customerId", ex.getMessage()));
    }

    @ExceptionHandler(NotEnoughProductInStockException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleNotEnughProductInStockException(NotEnoughProductInStockException ex) {
        return Collections.singletonList(new ErrorResponse("productId", ex.getMessage()));
    }

    @ExceptionHandler(ProductNotFoundInBombadilsEmporiumException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleProductNotFoundInBombadilsEmporiumException(ProductNotFoundInBombadilsEmporiumException ex) {
        return Collections.singletonList(new ErrorResponse("productId", ex.getMessage()));
    }

    @ExceptionHandler(NotEnoughProductInBombadilsEmporiumException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleNotEnoughProductInBombadilsEmporiumStockException(NotEnoughProductInBombadilsEmporiumException ex) {
        return Collections.singletonList(new ErrorResponse("productId", ex.getMessage()));
    }

    @ExceptionHandler(OrderNotFoundByIdException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleOrderNotFoundByIdException(OrderNotFoundByIdException ex) {
        return Collections.singletonList(new ErrorResponse("orderId", ex.getMessage()));
    }

    @ExceptionHandler(OrderNotDoneException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleOrderNotDoneException(OrderNotDoneException ex) {
        return Collections.singletonList(new ErrorResponse("orderId", ex.getMessage()));
    }

    @ExceptionHandler(EmptyCartException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleEmptyCartException(EmptyCartException ex) {
        return Collections.singletonList(new ErrorResponse("customerId", ex.getMessage()));
    }


    @ExceptionHandler(OrderNotExistsException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleOrderNotExistsException(OrderNotExistsException ex) {
        return Collections.singletonList(new ErrorResponse("orderId", ex.getMessage()));
    }

    @ExceptionHandler(OrderItemNotExistsException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleOrderItemNotExistsException(OrderItemNotExistsException ex) {
        return Collections.singletonList(new ErrorResponse("orderItemId", ex.getMessage()));
    }

    @ExceptionHandler(NotEnoughProductInWebshopException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleNotEnoughProductInWebshopException(NotEnoughProductInWebshopException ex) {
        return Collections.singletonList(new ErrorResponse("message", ex.getMessage()));
    }

    @ExceptionHandler(NotAuthorizedToViewThisPageException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleNotAuthorizedToViewThisPageException(NotAuthorizedToViewThisPageException ex) {
        return Collections.singletonList(new ErrorResponse("message", ex.getMessage()));
    }

    @ExceptionHandler(CustomerTypeMismatchException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleCustomerTypeMismatchException(CustomerTypeMismatchException ex) {
        return Collections.singletonList(new ErrorResponse("message", ex.getMessage()));
    }

    @ExceptionHandler(ProductNotInCartException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleProductNotInCartException(ProductNotInCartException ex) {
        return Collections.singletonList(new ErrorResponse("productId", ex.getMessage()));
    }

    @ExceptionHandler(CustomerHasNoAddressException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleCustomerHasNoAddressException(CustomerHasNoAddressException ex) {
        return Collections.singletonList(new ErrorResponse("addressId", ex.getMessage()));
    }

    @ExceptionHandler(ShippingAddressNotSetException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleShippingAddressNotSetException(ShippingAddressNotSetException ex) {
        return Collections.singletonList(new ErrorResponse("addressId", ex.getMessage()));
    }

    @ExceptionHandler(OrderNotNewException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleOrderNotNewException(OrderNotNewException ex) {
        return Collections.singletonList(new ErrorResponse("orderId", ex.getMessage()));
    }

    @ExceptionHandler(OrderNotBelongToCustomerException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleOrderNotBelongToCustomerException(OrderNotBelongToCustomerException ex) {
        return Collections.singletonList(new ErrorResponse("orderId", ex.getMessage()));
    }

    @ExceptionHandler(NoSubscribedCustomersException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleNoSubscribedCustomersException(NoSubscribedCustomersException ex) {
        return Collections.singletonList(new ErrorResponse("subscribers", ex.getMessage()));
    }

    @ExceptionHandler(TokenAlreadyConfirmedException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleTokenAlreadyConfirmedException(TokenAlreadyConfirmedException ex) {
        return Collections.singletonList(new ErrorResponse("message", ex.getMessage()));
    }

    @ExceptionHandler(TokenExpiredException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleTokenExpiredException(TokenExpiredException ex) {
        return Collections.singletonList(new ErrorResponse("message", ex.getMessage()));
    }

    @ExceptionHandler(TokenNotFoundException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleTokenNotFoundException(TokenNotFoundException ex) {
        return Collections.singletonList(new ErrorResponse("message", ex.getMessage()));
    }

    @ExceptionHandler(NoPromotionsFoundException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleNoPromotionsFoundException(NoPromotionsFoundException ex) {
        return Collections.singletonList(new ErrorResponse("promotions", ex.getMessage()));
    }

    @ExceptionHandler(ShippingMethodNotExistsException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleShippingMethodNotExistsException(ShippingMethodNotExistsException ex) {
        return Collections.singletonList(new ErrorResponse("shippingMethod", ex.getMessage()));
    }

    @ExceptionHandler(ProductCategoryInUseException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleProductCategoryInUseException(ProductCategoryInUseException ex) {
        return Collections.singletonList(new ErrorResponse("productCategoryId", ex.getMessage()));
    }

    @ExceptionHandler(PermissionDeniedForCustomerException.class)
    @ResponseStatus(FORBIDDEN)
    @ResponseBody
    public List<ErrorResponse> handlePermissionDeniedForCustomerException(PermissionDeniedForCustomerException ex) {
        return Collections.singletonList(new ErrorResponse("customerId", ex.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.error(ex.getMessage(), ex);
        return Collections.singletonList(new ErrorResponse("customerId", "User not found."));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handleUsernameNotFoundException(UsernameNotFoundException ex) {
        log.error(ex.getMessage(), ex);
        return new ErrorResponse("", "Username not found.");
    }

    @ExceptionHandler(PasswordsDoNotMatchException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public List<ErrorResponse> handlePasswordsDoNotMatchException(PasswordsDoNotMatchException ex) {
        return Collections.singletonList(new ErrorResponse("text", ex.getMessage()));
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handleAllOtherErrors(Throwable ex) {
        log.error(ex.getMessage(), ex);
        return new ErrorResponse("Internal Server Error", "Something went wrong.");
    }

}
