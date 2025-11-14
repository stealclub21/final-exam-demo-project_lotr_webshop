package hu.progmasters.webshop.service;

import hu.progmasters.webshop.domain.*;
import hu.progmasters.webshop.dto.incoming.*;
import hu.progmasters.webshop.dto.outgoing.ProductInfo;
import hu.progmasters.webshop.exception.NoPromotionsFoundException;
import hu.progmasters.webshop.exception.NotEnoughProductInStockException;
import hu.progmasters.webshop.exception.PermissionDeniedForCustomerException;
import hu.progmasters.webshop.exception.ProductNotExistsException;
import hu.progmasters.webshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.*;

import static hu.progmasters.webshop.domain.enumeration.CustomerType.ELF;
import static hu.progmasters.webshop.domain.enumeration.CustomerType.ORC;
import static hu.progmasters.webshop.domain.enumeration.ProductPromotionStatus.ON_PROMOTION;
import static hu.progmasters.webshop.domain.enumeration.Role.ROLE_ADMIN;
import static hu.progmasters.webshop.domain.enumeration.Role.ROLE_PREMIUM;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ImageUploadService imageUploadService;

    @Mock
    private ProductCategoryService productCategoryService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private CustomerService customerService;

    @Mock
    private ConnectionFixBetweenProductAndEmporiumService connectionFixBetweenProductAndEmporiumService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createProduct_withValidCommand_returnsProductInfo() {
        ProductCreateUpdateCommand command = new ProductCreateUpdateCommand();
        command.setProductCategory("Electronics");
        command.setImages(new ArrayList<>());
        when(imageUploadService.uploadImages(any())).thenReturn(new ArrayList<>());
        when(modelMapper.map(any(), any())).thenReturn(new Product());
        when(productCategoryService.getProductCategoryByName(anyString())).thenReturn(new ProductCategory());
        when(productRepository.save(any())).thenReturn(new Product());

        ProductInfo result = productService.createProduct(command);

        assertNotNull(result);
        verify(productRepository, times(1)).save(any());
    }

    @Test
    void createProduct_withNullCategory_setsDefaultCategory() {
        ProductCreateUpdateCommand command = new ProductCreateUpdateCommand();
        command.setProductCategory(null);
        command.setImages(new ArrayList<>());
        when(imageUploadService.uploadImages(any())).thenReturn(new ArrayList<>());
        when(modelMapper.map(any(), any())).thenReturn(new Product());
        when(productCategoryService.getProductCategoryByName(anyString())).thenReturn(new ProductCategory());
        when(productRepository.save(any())).thenReturn(new Product());

        ProductInfo result = productService.createProduct(command);

        assertNotNull(result);
        verify(productCategoryService, times(1)).getProductCategoryByName("Other");
    }

    @Test
    void createProduct_withNonExistingCategory_createsNewCategory() {
        ProductCreateUpdateCommand command = new ProductCreateUpdateCommand();
        command.setProductCategory("NonExistingCategory");
        command.setImages(new ArrayList<>());
        when(imageUploadService.uploadImages(any())).thenReturn(new ArrayList<>());
        when(modelMapper.map(any(), any())).thenReturn(new Product());
        when(productCategoryService.getProductCategoryByName(anyString())).thenReturn(null, new ProductCategory());
        when(productRepository.save(any())).thenReturn(new Product());

        ProductInfo result = productService.createProduct(command);

        assertNotNull(result);
        verify(productCategoryService, times(1)).addProductCategory("NonExistingCategory");
    }

    @Test
    void uploadImages_withNonExistingProduct_throwsException() {
        UploadFileCommand command = new UploadFileCommand();
        command.setImages(new ArrayList<>());
        Long productId = 1L;
        when(imageUploadService.uploadImages(any())).thenReturn(new ArrayList<>());

        assertThrows(ProductNotExistsException.class, () -> productService.uploadImages(command, productId));
    }

    @Test
    void uploadImages_withValidCommandAndProductId_updatesProductImages() {
        UploadFileCommand command = new UploadFileCommand();
        Product product = new Product();
        command.setImages(new ArrayList<>());
        Long productId = 1L;
        List<Image> mockImages = new ArrayList<>();
        mockImages.add(new Image());
        when(imageUploadService.uploadImages(any())).thenReturn(mockImages);
        when(productRepository.findById(anyLong())).thenReturn(java.util.Optional.of(product));
        product.setProductCategory(new ProductCategory());

        ProductInfo result = productService.uploadImages(command, productId);

        assertNotNull(result);
        assertEquals(1, result.getImageUrls().size());
    }

    @Test
    void updateProduct_withValidCommandAndProductId_updatesProduct() {
        ProductCreateUpdateCommand command = new ProductCreateUpdateCommand();
        Long productId = 1L;

        Customer customer = new Customer();
        customer.getRoles().add(ROLE_ADMIN);
        customer.setId(1L);

        Product product = new Product();
        product.setId(productId);

        ProductCategory productCategory = new ProductCategory();
        productCategory.setName("Test");

        ProductInfo productInfo = new ProductInfo();

        when(customerService.getLoggedInCustomer()).thenReturn(customer);
        when(connectionFixBetweenProductAndEmporiumService.isProductInEmporium(anyLong())).thenReturn(false);
        when(connectionFixBetweenProductAndEmporiumService.isProductCreatedByCustomer(anyLong(), anyLong())).thenReturn(false);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productCategoryService.getProductCategoryByName("Test")).thenReturn(productCategory);
        when(modelMapper.map(any(), any())).thenReturn(productInfo);

        ProductInfo result = productService.updateProduct(command, productId);

        assertNotNull(result);
    }

    @Test
    void updateProduct_ADMIN_withNonExistingProduct_throwsException() {
        ProductCreateUpdateCommand command = new ProductCreateUpdateCommand();
        Long productId = 1L;

        Customer customer = new Customer();
        customer.getRoles().add(ROLE_ADMIN);

        when(customerService.getLoggedInCustomer()).thenReturn(customer);
        when(connectionFixBetweenProductAndEmporiumService.isProductInEmporium(anyLong())).thenReturn(false);
        when(connectionFixBetweenProductAndEmporiumService.isProductCreatedByCustomer(anyLong(), anyLong())).thenReturn(false);
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ProductNotExistsException.class, () -> productService.updateProduct(command, productId));
    }

    @Test
    void updateProduct_PREMIUM_withExistingProduct_updatesProduct() {
        ProductCreateUpdateCommand command = new ProductCreateUpdateCommand();
        Long productId = 1L;

        Customer customer = new Customer();
        customer.setId(1L);
        customer.getRoles().add(ROLE_PREMIUM);

        when(customerService.getLoggedInCustomer()).thenReturn(customer);
        when(connectionFixBetweenProductAndEmporiumService.isProductInEmporium(anyLong())).thenReturn(true);
        when(connectionFixBetweenProductAndEmporiumService.isProductCreatedByCustomer(anyLong(), anyLong())).thenReturn(true);
        when(productCategoryService.getProductCategoryByName(anyString())).thenReturn(new ProductCategory());
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ProductNotExistsException.class, () -> productService.updateProduct(command, productId));
    }

    @Test
    void updateProduct_withNoPermission_throwsException() {
        ProductCreateUpdateCommand command = new ProductCreateUpdateCommand();
        Long productId = 1L;
        when(customerService.getLoggedInCustomer()).thenReturn(new Customer());
        when(connectionFixBetweenProductAndEmporiumService.isProductInEmporium(anyLong())).thenReturn(false);
        when(connectionFixBetweenProductAndEmporiumService.isProductCreatedByCustomer(anyLong(), anyLong())).thenReturn(false);

        assertThrows(PermissionDeniedForCustomerException.class, () -> productService.updateProduct(command, productId));
    }


    @Test
    void deleteProduct_withPremiumRoleAndExistingProductInEmporium_deletesProductFromEmporium() {
        Long productId = 1L;
        Customer premiumUser = new Customer();
        premiumUser.setId(1L);
        premiumUser.getRoles().add(ROLE_PREMIUM);

        when(customerService.getLoggedInCustomer()).thenReturn(premiumUser);
        when(connectionFixBetweenProductAndEmporiumService.isProductInEmporium(anyLong())).thenReturn(true);
        when(connectionFixBetweenProductAndEmporiumService.isProductCreatedByCustomer(anyLong(), anyLong())).thenReturn(true);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(new Product()));
        when(connectionFixBetweenProductAndEmporiumService.findBombadilsEmporiumByProductId(productId)).thenReturn(new BombadilsEmporium());

        String result = productService.deleteProduct(productId);

        assertEquals("Product deleted successfully!", result);
    }

    @Test
    void deleteProduct_withAdminRoleAndExistingProductInEmporium_deletesProductFromEmporium() {
        Long productId = 1L;
        Customer adminUser = new Customer();
        adminUser.getRoles().add(ROLE_ADMIN);
        BombadilsEmporium emporium = new BombadilsEmporium();
        when(customerService.getLoggedInCustomer()).thenReturn(adminUser);
        when(connectionFixBetweenProductAndEmporiumService.isProductInEmporium(anyLong())).thenReturn(true);
        when(connectionFixBetweenProductAndEmporiumService.isProductCreatedByCustomer(anyLong(), anyLong())).thenReturn(false);
        when(connectionFixBetweenProductAndEmporiumService.findBombadilsEmporiumByProductId(anyLong())).thenReturn(emporium);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(new Product()));

        String result = productService.deleteProduct(productId);

        assertEquals("Product deleted successfully!", result);
    }

    @Test
    void deleteProduct_withAdminRoleAndExistingProductNotInEmporium_deletesProduct() {
        Long productId = 1L;
        Customer customer = new Customer();
        customer.getRoles().add(ROLE_ADMIN);
        when(customerService.getLoggedInCustomer()).thenReturn(customer);
        when(connectionFixBetweenProductAndEmporiumService.isProductInEmporium(anyLong())).thenReturn(false);
        when(connectionFixBetweenProductAndEmporiumService.isProductCreatedByCustomer(anyLong(), anyLong())).thenReturn(false);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(new Product()));

        String result = productService.deleteProduct(productId);

        assertEquals("Product deleted successfully!", result);
    }

    @Test
    void deleteProduct_withNonExistingProduct_throwsException() {
        Long productId = 1L;
        Customer customer = new Customer();
        customer.getRoles().add(ROLE_ADMIN);
        when(customerService.getLoggedInCustomer()).thenReturn(customer);
        when(connectionFixBetweenProductAndEmporiumService.isProductInEmporium(anyLong())).thenReturn(false);
        when(connectionFixBetweenProductAndEmporiumService.isProductCreatedByCustomer(anyLong(), anyLong())).thenReturn(false);
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ProductNotExistsException.class, () -> productService.deleteProduct(productId));
    }

    @Test
    void deleteProduct_withNoPermission_throwsException() {
        Long productId = 1L;
        when(customerService.getLoggedInCustomer()).thenReturn(new Customer());
        when(connectionFixBetweenProductAndEmporiumService.isProductInEmporium(anyLong())).thenReturn(false);
        when(connectionFixBetweenProductAndEmporiumService.isProductCreatedByCustomer(anyLong(), anyLong())).thenReturn(false);

        assertThrows(PermissionDeniedForCustomerException.class, () -> productService.deleteProduct(productId));
    }

    @Test
    void addCategoryToProduct_withValidProductAndCategory_updatesProductCategory() {
        Long productId = 1L;
        Long productCategoryId = 1L;
        ProductInfo productInfo = new ProductInfo();
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(new Product()));
        when(productCategoryService.getProductCategoryById(anyLong())).thenReturn(new ProductCategory());
        when(modelMapper.map(any(), any())).thenReturn(productInfo);

        ProductInfo result = productService.addCategoryToProduct(productId, productCategoryId);

        assertNotNull(result);
        verify(productRepository, times(1)).save(any());
    }

    @Test
    void addCategoryToProduct_withNonExistingProduct_throwsException() {
        Long productId = 1L;
        Long productCategoryId = 1L;
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ProductNotExistsException.class, () -> productService.addCategoryToProduct(productId, productCategoryId));
    }

    @Test
    void listProducts_withNoDeletedProducts_returnsAllProducts() {
        List<Product> mockProducts = new ArrayList<>();
        Product product1 = new Product();
        product1.setId(1L);
        Product product2 = new Product();
        product2.setId(2L);
        mockProducts.add(product1);
        mockProducts.add(product2);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        when(productRepository.findAllNonDeletedProducts()).thenReturn(mockProducts);

        List<ProductInfo> result = productService.listProducts();

        assertEquals(2, result.size());
    }


    @Test
    void listProducts_whenAllProductsDeletedExist_returnsEmptyList() {
        when(productRepository.findAllNonDeletedProducts()).thenReturn(new ArrayList<>());
        List<ProductInfo> result = productService.listProducts();
        assertTrue(result.isEmpty());
    }

    @Test
    void getProductById_withValidId_returnsProductInfo() {
        Long productId = 1L;
        Product mockProduct = new Product();
        mockProduct.setId(productId);
        mockProduct.setProductCategory(new ProductCategory());
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        ProductInfo result = productService.getProductById(productId);

        assertNotNull(result);
        assertEquals(productId, result.getId());
    }

    @Test
    void getProductById_withInvalidId_throwsProductNotExistsException() {
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotExistsException.class, () -> productService.getProductById(productId));
    }

    @Test
    void getProductsByCategory_withInvalidCategoryId_returnsEmptyList() {
        Long productCategoryId = 1L;
        when(productRepository.findAllByProductCategoryId(productCategoryId)).thenReturn(new ArrayList<>());

        List<ProductInfo> result = productService.getProductsByCategory(productCategoryId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getProductsByName_withValidName_returnsProductInfos() {
        GetProductByName command = new GetProductByName();
        command.setName("ValidName");
        List<Product> mockProducts = new ArrayList<>();
        Product product1 = new Product();
        product1.setId(1L);
        Product product2 = new Product();
        product2.setId(2L);
        mockProducts.add(product1);
        mockProducts.add(product2);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        when(productRepository.findAllByName(command.getName())).thenReturn(mockProducts);

        List<ProductInfo> result = productService.getProductsByName(command);

        assertEquals(2, result.size());
    }

    @Test
    void getProductsByName_withInvalidName_returnsEmptyList() {
        GetProductByName command = new GetProductByName();
        command.setName("InvalidName");
        when(productRepository.findAllByName(command.getName())).thenReturn(new ArrayList<>());

        List<ProductInfo> result = productService.getProductsByName(command);

        assertTrue(result.isEmpty());
    }

    @Test
    void addTags_withValidCommandAndProductId_addsTagsToProduct() {
        TagsCreateDeleteCommand command = new TagsCreateDeleteCommand();
        command.setTags("tag1");

        ProductCategory productCategory = new ProductCategory();
        productCategory.setName("Test");

        Long productId = 1L;
        Product mockProduct = new Product();
        mockProduct.setId(productId);
        mockProduct.setProductCategory(productCategory);

        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        ProductInfo result = productService.addTags(command, productId);

        assertNotNull(result);
        assertEquals(1, result.getTags().size());
    }

    @Test
    void addTags_withEmptyCommandAndValidProductId_doesNotAddTagsToProduct() {
        TagsCreateDeleteCommand command = new TagsCreateDeleteCommand();
        command.setTags(" ");

        ProductCategory productCategory = new ProductCategory();
        productCategory.setName(anyString());

        Long productId = 1L;
        Product mockProduct = new Product();
        mockProduct.setId(productId);
        mockProduct.setProductCategory(productCategory);

        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        ProductInfo result = productService.addTags(command, productId);

        assertNotNull(result);
        assertTrue(result.getTags().isEmpty());
    }

    @Test
    void addTags_withValidCommandAndInvalidProductId_throwsProductNotExistsException() {
        TagsCreateDeleteCommand command = new TagsCreateDeleteCommand();
        command.setTags("tag1");
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotExistsException.class, () -> productService.addTags(command, productId));
    }

    @Test
    void deleteTags_withValidCommandAndInvalidProductId_throwsProductNotExistsException() {
        TagsCreateDeleteCommand command = new TagsCreateDeleteCommand();
        command.setTags("tag1, tag2");
        Long productId = 1L;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotExistsException.class, () -> productService.deleteTags(command, productId));
    }

    @Test
    void addToStock_withValidCommandAndPremiumUser_updatesStock() {
        UpdateStockCommand command = new UpdateStockCommand();
        command.setProductId(1L);
        command.setAmount(10);

        Customer customer = new Customer();
        customer.getRoles().add(ROLE_PREMIUM);
        customer.setId(1L);

        when(customerService.getLoggedInCustomer()).thenReturn(customer);
        when(connectionFixBetweenProductAndEmporiumService.isProductInEmporium(command.getProductId())).thenReturn(true);
        when(connectionFixBetweenProductAndEmporiumService.isProductCreatedByCustomer(customer.getId(), command.getProductId())).thenReturn(true);

        Product product = new Product();
        product.setId(command.getProductId());
        product.setInStock(5);

        when(productRepository.findById(command.getProductId())).thenReturn(Optional.of(product));

        String result = productService.addToStock(command);

        assertEquals("Stock updated successfully!", result);
        assertEquals(15, product.getInStock());
    }

    @Test
    void addToStock_withValidCommandAndAdminUser_updatesStock() {
        UpdateStockCommand command = new UpdateStockCommand();
        command.setProductId(1L);
        command.setAmount(10);

        Customer customer = new Customer();
        customer.getRoles().add(ROLE_ADMIN);
        customer.setId(1L);

        when(customerService.getLoggedInCustomer()).thenReturn(customer);

        Product product = new Product();
        product.setId(command.getProductId());
        product.setInStock(5);

        when(productRepository.findById(command.getProductId())).thenReturn(Optional.of(product));

        String result = productService.addToStock(command);

        assertEquals("Stock updated successfully!", result);
        assertEquals(15, product.getInStock());
    }

    @Test
    void addToStock_withInvalidProductId_throwsProductNotExistsException() {
        UpdateStockCommand command = new UpdateStockCommand();
        command.setProductId(1L);
        command.setAmount(10);

        Customer customer = new Customer();
        customer.getRoles().add(ROLE_ADMIN);
        customer.setId(1L);

        when(customerService.getLoggedInCustomer()).thenReturn(customer);
        when(productRepository.findById(command.getProductId())).thenReturn(Optional.empty());

        assertThrows(ProductNotExistsException.class, () -> productService.addToStock(command));
    }

    @Test
    void addToStock_withNoPermission_throwsPermissionDeniedForCustomerException() {
        UpdateStockCommand command = new UpdateStockCommand();
        command.setProductId(1L);
        command.setAmount(10);

        Customer customer = new Customer();
        customer.setId(1L);

        when(customerService.getLoggedInCustomer()).thenReturn(customer);
        when(connectionFixBetweenProductAndEmporiumService.isProductInEmporium(command.getProductId())).thenReturn(false);
        when(connectionFixBetweenProductAndEmporiumService.isProductCreatedByCustomer(customer.getId(), command.getProductId())).thenReturn(false);

        assertThrows(PermissionDeniedForCustomerException.class, () -> productService.addToStock(command));
    }

    @Test
    void removeFromStock_withValidCommandAndPremiumUser_updatesStock() {
        UpdateStockCommand command = new UpdateStockCommand();
        command.setProductId(1L);
        command.setAmount(5);

        Customer customer = new Customer();
        customer.getRoles().add(ROLE_PREMIUM);
        customer.setId(1L);

        when(customerService.getLoggedInCustomer()).thenReturn(customer);
        when(connectionFixBetweenProductAndEmporiumService.isProductInEmporium(command.getProductId())).thenReturn(true);
        when(connectionFixBetweenProductAndEmporiumService.isProductCreatedByCustomer(customer.getId(), command.getProductId())).thenReturn(true);

        Product product = new Product();
        product.setId(command.getProductId());
        product.setInStock(10);

        when(productRepository.findById(command.getProductId())).thenReturn(Optional.of(product));

        String result = productService.removeFromStock(command);

        assertEquals("Stock updated successfully!", result);
        assertEquals(5, product.getInStock());
    }

    @Test
    void removeFromStock_withValidCommandAndAdminUser_updatesStock() {
        UpdateStockCommand command = new UpdateStockCommand();
        command.setProductId(1L);
        command.setAmount(5);

        Customer customer = new Customer();
        customer.getRoles().add(ROLE_ADMIN);
        customer.setId(1L);

        when(customerService.getLoggedInCustomer()).thenReturn(customer);

        Product product = new Product();
        product.setId(command.getProductId());
        product.setInStock(10);

        when(productRepository.findById(command.getProductId())).thenReturn(Optional.of(product));

        String result = productService.removeFromStock(command);

        assertEquals("Stock updated successfully!", result);
        assertEquals(5, product.getInStock());
    }

    @Test
    void removeFromStock_withInvalidProductId_throwsProductNotExistsException() {
        UpdateStockCommand command = new UpdateStockCommand();
        command.setProductId(1L);
        command.setAmount(5);

        Customer customer = new Customer();
        customer.getRoles().add(ROLE_ADMIN);
        customer.setId(1L);

        when(customerService.getLoggedInCustomer()).thenReturn(customer);
        when(productRepository.findById(command.getProductId())).thenReturn(Optional.empty());

        assertThrows(ProductNotExistsException.class, () -> productService.removeFromStock(command));
    }

    @Test
    void removeFromStock_withNoPermission_throwsPermissionDeniedForCustomerException() {
        UpdateStockCommand command = new UpdateStockCommand();
        command.setProductId(1L);
        command.setAmount(5);

        Customer customer = new Customer();
        customer.setId(1L);

        when(customerService.getLoggedInCustomer()).thenReturn(customer);
        when(connectionFixBetweenProductAndEmporiumService.isProductInEmporium(command.getProductId())).thenReturn(false);
        when(connectionFixBetweenProductAndEmporiumService.isProductCreatedByCustomer(customer.getId(), command.getProductId())).thenReturn(false);

        assertThrows(PermissionDeniedForCustomerException.class, () -> productService.removeFromStock(command));
    }

    @Test
    void removeFromStock_withNotEnoughStock_throwsNotEnoughProductInStockException() {
        UpdateStockCommand command = new UpdateStockCommand();
        command.setProductId(1L);
        command.setAmount(15);

        Customer customer = new Customer();
        customer.getRoles().add(ROLE_ADMIN);
        customer.setId(1L);

        when(customerService.getLoggedInCustomer()).thenReturn(customer);

        Product product = new Product();
        product.setId(command.getProductId());
        product.setInStock(10);

        when(productRepository.findById(command.getProductId())).thenReturn(Optional.of(product));

        assertThrows(NotEnoughProductInStockException.class, () -> productService.removeFromStock(command));
    }

    @Test
    void addCustomerTypeToProduct_withValidCommand_setsCustomerType() {
        AddCustomerTypeToProductCommand command = new AddCustomerTypeToProductCommand();
        command.setProductId(1L);
        command.setCustomerType(ORC);

        Product product = new Product();
        product.setId(command.getProductId());

        when(productRepository.findById(command.getProductId())).thenReturn(Optional.of(product));

        String result = productService.addCustomerTypeToProduct(command);

        assertEquals("Customer type added successfully!", result);
        assertEquals(ORC, product.getCustomerType());
    }

    @Test
    void addCustomerTypeToProduct_withInvalidProductId_throwsProductNotExistsException() {
        AddCustomerTypeToProductCommand command = new AddCustomerTypeToProductCommand();
        command.setProductId(1L);
        command.setCustomerType(ELF);

        when(productRepository.findById(command.getProductId())).thenReturn(Optional.empty());

        assertThrows(ProductNotExistsException.class, () -> productService.addCustomerTypeToProduct(command));
    }

    @Test
    void getAllProductsOnPromotion_withProductsOnPromotion_returnsProductInfos() {
        // Given
        List<Product> mockProducts = new ArrayList<>();
        Product product1 = new Product();
        product1.setId(1L);
        product1.setPromotionStatus(ON_PROMOTION);
        Product product2 = new Product();
        product2.setId(2L);
        product2.setPromotionStatus(ON_PROMOTION);
        mockProducts.add(product1);
        mockProducts.add(product2);

        when(productRepository.findAllByPromotion()).thenReturn(Optional.of(mockProducts));

        List<ProductInfo> result = productService.getAllProductsOnPromotion();

        assertEquals(2, result.size());
    }

    @Test
    void getAllProductsOnPromotion_withNoProductsOnPromotion_throwsNoPromotionsFoundException() {
        when(productRepository.findAllByPromotion()).thenReturn(Optional.empty());

        assertThrows(NoPromotionsFoundException.class, () -> productService.getAllProductsOnPromotion());
    }

}