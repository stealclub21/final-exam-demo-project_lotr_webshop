package hu.progmasters.webshop.service;

import hu.progmasters.webshop.domain.Product;
import hu.progmasters.webshop.domain.Rating;
import hu.progmasters.webshop.dto.incoming.RatingCreateCommand;
import hu.progmasters.webshop.dto.outgoing.RatingInfo;
import hu.progmasters.webshop.exception.CustomerDidNotOrderProductException;
import hu.progmasters.webshop.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createRating_whenValidCommandAndCustomerOrderedProduct_createsRating() {
        Long customerId = 1L;
        Long productId = 1L;
        RatingCreateCommand command = new RatingCreateCommand();
        command.setProductId(productId);

        Product product = new Product();
        when(orderService.didCustomerOrderProduct(customerId, productId)).thenReturn(product);

        Rating rating = new Rating();
        when(modelMapper.map(any(RatingCreateCommand.class), eq(Rating.class))).thenReturn(rating);

        RatingInfo ratingInfo = new RatingInfo();
        when(modelMapper.map(any(Rating.class), eq(RatingInfo.class))).thenReturn(ratingInfo);

        RatingInfo result = ratingService.createRating(command, customerId);

        assertNotNull(result, "Result should not be null");
    }

    @Test
    void createRating_whenValidCommandAndCustomerDidNotOrderProduct_throwsException() {
        Long customerId = 1L;
        Long productId = 1L;
        RatingCreateCommand command = new RatingCreateCommand();
        command.setProductId(productId);

        when(orderService.didCustomerOrderProduct(customerId, productId)).thenReturn(null);

        assertThrows(CustomerDidNotOrderProductException.class, () -> ratingService.createRating(command, customerId));
    }

}