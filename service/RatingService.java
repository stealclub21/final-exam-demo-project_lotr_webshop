package hu.progmasters.webshop.service;

import hu.progmasters.webshop.domain.Product;
import hu.progmasters.webshop.domain.Rating;
import hu.progmasters.webshop.dto.incoming.RatingCreateCommand;
import hu.progmasters.webshop.dto.outgoing.RatingInfo;
import hu.progmasters.webshop.exception.CustomerDidNotOrderProductException;
import hu.progmasters.webshop.repository.RatingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;


@Service
@Transactional
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final OrderService orderService;
    private final ModelMapper modelMapper;

    public RatingInfo createRating(RatingCreateCommand command, Long customerId) {

        Long productId = command.getProductId();
        Product product = orderService.didCustomerOrderProduct(customerId, productId);

        if (product == null) {
            throw new CustomerDidNotOrderProductException(customerId, productId);
        }

        Rating rating = setRating(command, customerId, product);
        ratingRepository.save(rating);
        return getRatingInfo(rating, productId);
    }

    private RatingInfo getRatingInfo(Rating rating, Long productId) {
        RatingInfo ratingInfo = modelMapper.map(rating, RatingInfo.class);
        ratingInfo.setProductId(productId);
        return ratingInfo;
    }

    private Rating setRating(RatingCreateCommand command, Long customerId, Product product) {
        Rating rating = modelMapper.map(command, Rating.class);
        rating.setCustomerId(customerId);
        rating.setProduct(product);
        return rating;
    }
}
