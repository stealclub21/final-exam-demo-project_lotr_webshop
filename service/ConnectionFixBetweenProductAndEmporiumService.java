package hu.progmasters.webshop.service;

import hu.progmasters.webshop.domain.BombadilsEmporium;
import hu.progmasters.webshop.exception.ProductNotFoundInBombadilsEmporiumException;
import hu.progmasters.webshop.repository.BombadilsEmporiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConnectionFixBetweenProductAndEmporiumService {
    private final BombadilsEmporiumRepository bombadilsEmporiumRepository;

    public boolean isProductCreatedByCustomer(Long customerId, Long productId) {
        return bombadilsEmporiumRepository.existsByCustomerIdAndProductId(customerId, productId);
    }

    public boolean isProductInEmporium(Long productId) {
        return bombadilsEmporiumRepository.existsActiveByProductId(productId);
    }

    public BombadilsEmporium findBombadilsEmporiumByProductId(Long productId) {
        return bombadilsEmporiumRepository.findActiveByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundInBombadilsEmporiumException(productId));
    }
}
