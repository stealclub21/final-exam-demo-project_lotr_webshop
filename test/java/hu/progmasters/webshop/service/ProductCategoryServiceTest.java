package hu.progmasters.webshop.service;

import hu.progmasters.webshop.domain.ProductCategory;
import hu.progmasters.webshop.dto.incoming.ProductCategoryCreateUpdateCommand;
import hu.progmasters.webshop.dto.outgoing.ProductCategoryInfo;
import hu.progmasters.webshop.exception.ProductCategoryAlreadyExistsException;
import hu.progmasters.webshop.exception.ProductCategoryNotExistsException;
import hu.progmasters.webshop.repository.ProductCategoryRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ProductCategoryServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProductCategoryService productCategoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addProductCategory_whenValidCommand_addsProductCategory() {
        ProductCategoryCreateUpdateCommand command = new ProductCategoryCreateUpdateCommand();
        command.setName("Electronics");

        ProductCategory productCategory = new ProductCategory();
        productCategory.setName(command.getName());

        when(modelMapper.map(command, ProductCategory.class)).thenReturn(productCategory);
        when(productCategoryRepository.save(productCategory)).thenReturn(productCategory);

        ProductCategoryInfo productCategoryInfo = new ProductCategoryInfo();
        productCategoryInfo.setName(command.getName());
        when(modelMapper.map(productCategory, ProductCategoryInfo.class)).thenReturn(productCategoryInfo);

        ProductCategoryInfo result = productCategoryService.addProductCategory(command);

        assertNotNull(result, "Result should not be null");

        assertEquals(command.getName(), result.getName());
    }

    @Test
    void addProductCategory_whenDuplicateName_throwsException() {
        ProductCategoryCreateUpdateCommand command = new ProductCategoryCreateUpdateCommand();
        command.setName("Electronics");

        when(modelMapper.map(any(ProductCategoryCreateUpdateCommand.class), eq(ProductCategory.class))).thenReturn(new ProductCategory());
        when(productCategoryRepository.save(any(ProductCategory.class))).thenThrow(new DataIntegrityViolationException(""));

        assertThrows(ProductCategoryAlreadyExistsException.class, () -> productCategoryService.addProductCategory(command));
    }

    @Test
    void deleteProductCategory_whenValidId_deletesProductCategory() {
        Long validId = 1L;
        ProductCategory productCategory = new ProductCategory();
        productCategory.setId(validId);
        productCategory.setName("Electronics");

        when(productCategoryRepository.findById(validId)).thenReturn(Optional.of(productCategory));
        entityManager.flush();

        String result = productCategoryService.deleteProductCategory(validId);

        assertEquals("Product category with id " + validId + " deleted successfully", result);
    }

    @Test
    void deleteProductCategory_whenInvalidId_throwsProductCategoryNotExistsException() {
        Long invalidId = 2L;
        when(productCategoryRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(ProductCategoryNotExistsException.class, () -> productCategoryService.deleteProductCategory(invalidId));
    }

    @Test
    void getProductCategoryByName_whenValidName_returnsProductCategory() {
        String validName = "Electronics";
        ProductCategory productCategory = new ProductCategory();
        productCategory.setName(validName);

        when(productCategoryRepository.findByName(validName)).thenReturn(productCategory);

        ProductCategory result = productCategoryService.getProductCategoryByName(validName);

        assertEquals(validName, result.getName());
    }

    @Test
    void getProductCategoryByName_whenInvalidName_returnsNull() {
        String invalidName = "Invalid";
        when(productCategoryRepository.findByName(invalidName)).thenReturn(null);

        ProductCategory result = productCategoryService.getProductCategoryByName(invalidName);

        assertNull(result);
    }

    @Test
    void addProductCategory_whenValidName_addsProductCategory() {
        String validName = "Electronics";
        ProductCategory productCategory = new ProductCategory();
        productCategory.setName(validName);

        when(productCategoryRepository.save(any(ProductCategory.class))).thenReturn(productCategory);

        ProductCategory result = productCategoryService.addProductCategory(validName);

        assertEquals(validName, result.getName());
    }

    @Test
    void getProductCategoryById_whenValidId_returnsProductCategory() {
        Long validId = 1L;
        ProductCategory productCategory = new ProductCategory();
        productCategory.setId(validId);

        when(productCategoryRepository.findById(validId)).thenReturn(Optional.of(productCategory));

        ProductCategory result = productCategoryService.getProductCategoryById(validId);

        assertEquals(validId, result.getId());
    }

    @Test
    void getProductCategoryById_whenInvalidId_throwsProductCategoryNotExistsException() {
        Long invalidId = 2L;
        when(productCategoryRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(ProductCategoryNotExistsException.class, () -> productCategoryService.getProductCategoryById(invalidId));
    }

}