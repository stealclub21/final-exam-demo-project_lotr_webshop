package hu.progmasters.webshop.service;

import hu.progmasters.webshop.domain.ProductCategory;
import hu.progmasters.webshop.dto.incoming.ProductCategoryCreateUpdateCommand;
import hu.progmasters.webshop.dto.outgoing.ProductCategoryInfo;
import hu.progmasters.webshop.exception.ProductCategoryAlreadyExistsException;
import hu.progmasters.webshop.exception.ProductCategoryNotExistsException;
import hu.progmasters.webshop.repository.ProductCategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductCategoryService {

    @PersistenceContext
    private final EntityManager entityManager;

    private final ProductCategoryRepository productCategoryRepository;
    private final ModelMapper modelMapper;

    public ProductCategoryInfo addProductCategory(ProductCategoryCreateUpdateCommand command) {

        ProductCategory productCategory = modelMapper.map(command, ProductCategory.class);

        try {
            productCategoryRepository.save(productCategory);
        } catch (Exception e) {
            throw new ProductCategoryAlreadyExistsException(command.getName());
        }
        return modelMapper.map(productCategory, ProductCategoryInfo.class);
    }

    public String deleteProductCategory(Long productCategoryId) {
        ProductCategory productCategory = getProductCategoryById(productCategoryId);

        try {
            productCategoryRepository.delete(productCategory);
            entityManager.flush();
        } catch (Exception e) {
            throw new ProductCategoryNotExistsException(productCategoryId);
        }
        return "Product category with id " + productCategoryId + " deleted successfully";
    }

    public ProductCategory getProductCategoryByName(String name) {
        return productCategoryRepository.findByName(name);
    }

    public ProductCategory addProductCategory(String productCategoryName) {
        ProductCategory productCategory = new ProductCategory(productCategoryName);
        productCategoryRepository.save(productCategory);
        return productCategory;
    }

    public ProductCategory getProductCategoryById(Long productCategoryId) {
        return productCategoryRepository.
                findById(productCategoryId)
                .orElseThrow(() -> new ProductCategoryNotExistsException(productCategoryId));
    }
}
