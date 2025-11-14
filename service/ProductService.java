package hu.progmasters.webshop.service;

import hu.progmasters.webshop.domain.*;
import hu.progmasters.webshop.domain.enumeration.Role;
import hu.progmasters.webshop.dto.incoming.*;
import hu.progmasters.webshop.dto.outgoing.ProductCategoryInfo;
import hu.progmasters.webshop.dto.outgoing.ProductInfo;
import hu.progmasters.webshop.exception.*;
import hu.progmasters.webshop.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static hu.progmasters.webshop.domain.enumeration.ProductPromotionStatus.NOT_ON_PROMOTION;
import static hu.progmasters.webshop.domain.enumeration.ProductPromotionStatus.ON_PROMOTION;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryService productCategoryService;
    private final ImageUploadService imageUploadService;
    private final CustomerService customerService;
    private final ModelMapper modelMapper;
    private final ConnectionFixBetweenProductAndEmporiumService connectionFixBetweenProductAndEmporiumService;

    public ProductInfo createProduct(ProductCreateUpdateCommand command) {

        checkIfProductAlreadyExists(command);
        List<Image> images = imageUploadService.uploadImages(command.getImages());

        Product product = modelMapper.map(command, Product.class);

        if (!images.isEmpty()) {
            product.setImages(images);
            images.forEach(image -> image.setProduct(product));
        }

        String productCategoryName = command.getProductCategory();

        if (productCategoryName == null || productCategoryName.isBlank()) {
            productCategoryName = "Other";
        }
        ProductCategory productCategory = productCategoryService.getProductCategoryByName(productCategoryName);

        ProductInfo productInfo = new ProductInfo(product);

        if (productCategory == null) {
            productCategoryService.addProductCategory(productCategoryName);
            productCategory = productCategoryService.getProductCategoryByName(productCategoryName);
        }
        product.setProductCategory(productCategory);
        setProductCategoryToProductInfo(productInfo, productCategory);

        productRepository.save(product);
        productInfo.setId(product.getId());

        return productInfo;
    }

    public ProductInfo uploadImages(UploadFileCommand command, Long productId) {

        Product product = findProductById(productId);
        List<Image> images = imageUploadService.uploadImages(command.getImages());

        if (!images.isEmpty()) {
            product.setImages(images);
            images.forEach(image -> image.setProduct(product));
        }

        ProductInfo productInfo = new ProductInfo(product);
        setProductCategoryToProductInfo(productInfo, product.getProductCategory());
        return productInfo;
    }

    public ProductInfo updateProduct(ProductCreateUpdateCommand command, Long productId) {
        Customer customer = customerService.getLoggedInCustomer();
        boolean isProductInEmporium = connectionFixBetweenProductAndEmporiumService.isProductInEmporium(productId);
        boolean isCustomerPremium = customer.getRoles().contains(Role.ROLE_PREMIUM);
        boolean isProductCreatedByCustomer = connectionFixBetweenProductAndEmporiumService.isProductCreatedByCustomer(customer.getId(), productId);

        if (isProductInEmporium
                && isCustomerPremium
                && isProductCreatedByCustomer
                || customer.getRoles().contains(Role.ROLE_ADMIN)) {

            Product product = findProductById(productId);
            ProductCategory productCategory = getOrCreateProductCategory(command);

            modelMapper.map(command, product);
            product.setProductCategory(productCategory);

            ProductInfo productInfo = modelMapper.map(product, ProductInfo.class);

            try {
                setProductCategoryToProductInfo(productInfo, productCategory);
            } catch (NullPointerException e) {
                e.getMessage();
            }
            productRepository.save(product);
            return productInfo;
        } else {
            throw new PermissionDeniedForCustomerException(customer.getId());
        }
    }

    public String deleteProduct(Long productId) {
        Customer customer = customerService.getLoggedInCustomer();
        boolean isProductInEmporium = connectionFixBetweenProductAndEmporiumService.isProductInEmporium(productId);
        boolean isCustomerPremium = customer.getRoles().contains(Role.ROLE_PREMIUM);
        boolean isProductCreatedByCustomer = connectionFixBetweenProductAndEmporiumService.isProductCreatedByCustomer(customer.getId(), productId);

        if (isProductInEmporium
                && isCustomerPremium
                && isProductCreatedByCustomer
                || customer.getRoles().contains(Role.ROLE_ADMIN)) {

                Product product = findProductById(productId);
                product.setDeleted(true);
                return "Product deleted successfully!";

        } else {
            throw new PermissionDeniedForCustomerException(customer.getId());
        }
    }

    public ProductInfo addCategoryToProduct(Long productId, Long productCategoryId) {

        Product product = findProductById(productId);
        ProductCategory productCategory = productCategoryService.getProductCategoryById(productCategoryId);

        product.setProductCategory(productCategory);
        productRepository.save(product);

        ProductInfo productInfo = modelMapper.map(product, ProductInfo.class);
        setProductCategoryToProductInfo(productInfo, productCategory);
        return productInfo;
    }

    public List<ProductInfo> listProducts() {

        List<Product> products = productRepository.findAllNonDeletedProducts();
        List<ProductInfo> productInfos = getProductInfos(products);

        try {
            setProductCategoriesToListOfProductInfosWithoutProductCategoryId(products, productInfos);
        } catch (NullPointerException e) {
            e.getMessage();
        }
        setRatingsToMultipleProductInfos(productInfos);
        return productInfos;
    }

    public ProductInfo getProductById(Long productId) {

        Product product = findProductById(productId);
        ProductInfo productInfo = new ProductInfo(product);
        setProductCategoryToProductInfo(productInfo, product.getProductCategory());

        List<Rating> ratings = orderRatingsRatingDateDesc(product);
        productInfo.setRatings(ratings);

        return productInfo;
    }

    public List<ProductInfo> getProductsByCategory(Long productCategoryId) {

        List<Product> products = productRepository.findAllByProductCategoryId(productCategoryId);
        List<ProductInfo> productInfos = getProductInfos(products);

        setProductCategoriesToListOfProductInfosWithProductId(productCategoryId, productInfos);

        setRatingsToMultipleProductInfos(productInfos);
        return productInfos;
    }

    public List<ProductInfo> getProductsByName(GetProductByName command) {

        List<Product> products = productRepository.findAllByName(command.getName());
        List<ProductInfo> productInfos = getProductInfos(products);

        try {
            setProductCategoriesToListOfProductInfosWithoutProductCategoryId(products, productInfos);
        } catch (NullPointerException e) {
            e.getMessage();
        }
        setRatingsToMultipleProductInfos(productInfos);
        return productInfos;
    }

    public ProductInfo addTags(TagsCreateDeleteCommand command, Long productId) {

        Product product = findProductById(productId);

        Set<String> tags = getTagsFromCommandAndTransformToSet(command);

        product.getTags().addAll(tags);
        productRepository.save(product);
        ProductInfo productInfo = new ProductInfo(product);
        ProductCategory productCategory = product.getProductCategory();
        setProductCategoryToProductInfo(productInfo, productCategory);
        return productInfo;
    }

    public ProductInfo deleteTags(TagsCreateDeleteCommand command, Long productId) {

        Product product = findProductById(productId);
        Set<String> tagsExisting = product.getTags();

        Set<String> tags = getTagsFromCommandAndTransformToSet(command);

        tagsExisting.removeAll(tags);
        product.setTags(tags);
        productRepository.save(product);

        ProductInfo productInfo = new ProductInfo(product);
        ProductCategory productCategory = product.getProductCategory();
        setProductCategoryToProductInfo(productInfo, productCategory);
        return productInfo;
    }

    private Set<String> getTagsFromCommandAndTransformToSet(TagsCreateDeleteCommand command) {
        String[] tagsArray = command.getTags().split(", ");
        return Arrays.stream(tagsArray)
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toSet());
    }

    private void setRatingsToMultipleProductInfos(List<ProductInfo> productInfos) {
        productInfos.forEach(productInfo -> {
            List<Rating> ratings = orderRatingsRatingDateDesc(findProductById(productInfo.getId()));
            productInfo.setRatings(ratings);
        });
    }

    private List<Rating> orderRatingsRatingDateDesc(Product product) {
        List<Rating> ratings = product.getRatings();
        ratings.sort((r1, r2) -> r2.getRatingDate().compareTo(r1.getRatingDate()));
        return ratings;
    }

    private void setProductCategoriesToListOfProductInfosWithoutProductCategoryId(List<Product> products, List<ProductInfo> productInfos) {
        products.forEach(product -> {
            ProductCategory productCategory = product.getProductCategory();
            if (productCategory != null) {
                ProductInfo productInfo = productInfos.get(products.indexOf(product));
                setProductCategoryToProductInfo(productInfo, productCategory);
            }
        });
    }

    public Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotExistsException(productId));
    }

    public String addToStock(UpdateStockCommand command) {
        Customer customer = customerService.getLoggedInCustomer();
        boolean isProductInEmporium = connectionFixBetweenProductAndEmporiumService.isProductInEmporium(command.getProductId());
        boolean isCustomerPremium = customer.getRoles().contains(Role.ROLE_PREMIUM);
        boolean isProductCreatedByCustomer = connectionFixBetweenProductAndEmporiumService.isProductCreatedByCustomer(customer.getId(), command.getProductId());

        if (isProductInEmporium
                && isCustomerPremium
                && isProductCreatedByCustomer
                || customer.getRoles().contains(Role.ROLE_ADMIN)) {
            Product product = findProductById(command.getProductId());
            product.setInStock(product.getInStock() + command.getAmount());
            return "Stock updated successfully!";
        } else {
            throw new PermissionDeniedForCustomerException(customer.getId());
        }

    }

    public String removeFromStock(UpdateStockCommand command) {
        Customer customer = customerService.getLoggedInCustomer();
        boolean isProductInEmporium = connectionFixBetweenProductAndEmporiumService.isProductInEmporium(command.getProductId());
        boolean isCustomerPremium = customer.getRoles().contains(Role.ROLE_PREMIUM);
        boolean isProductCreatedByCustomer = connectionFixBetweenProductAndEmporiumService.isProductCreatedByCustomer(customer.getId(), command.getProductId());

        if (isProductInEmporium
                && isCustomerPremium
                && isProductCreatedByCustomer
                || customer.getRoles().contains(Role.ROLE_ADMIN)) {
            Product product = findProductById(command.getProductId());
            if (product.getInStock() - command.getAmount() < 0) {
                throw new NotEnoughProductInStockException(product.getId());
            }
            product.setInStock(product.getInStock() - command.getAmount());
            return "Stock updated successfully!";
        } else {
            throw new PermissionDeniedForCustomerException(customer.getId());
        }
    }

    private void setProductCategoriesToListOfProductInfosWithProductId(Long productCategoryId, List<ProductInfo> productInfos) {
        productInfos.forEach(productInfo -> {
            ProductCategory productCategory = productCategoryService.getProductCategoryById(productCategoryId);
            setProductCategoryToProductInfo(productInfo, productCategory);
        });
    }

    private void setProductCategoryToProductInfo(ProductInfo productInfo, ProductCategory productCategory) {
        ProductCategoryInfo productCategoryInfo = new ProductCategoryInfo(productCategory.getName());
        productInfo.setProductCategoryInfo(productCategoryInfo);
    }

    private ProductCategory getOrCreateProductCategory(ProductCreateUpdateCommand command) {
        String productCategoryName = command.getProductCategory();
        ProductCategory productCategory;

        if (productCategoryService.getProductCategoryByName(productCategoryName) == null) {
            productCategory = productCategoryService.addProductCategory(productCategoryName);

        } else {
            productCategory = productCategoryService.getProductCategoryByName(productCategoryName);
        }
        return productCategory;
    }

    private void checkIfProductAlreadyExists(ProductCreateUpdateCommand command) {
        String name = command.getName();
        String vendor = command.getVendor();
        Product existingProduct = productRepository.findByNameAndVendor(name, vendor);

        if (existingProduct != null) {
            throw new ProductAlreadyExistsException(name, vendor);
        }
    }

    private List<ProductInfo> getProductInfos(List<Product> products) {

        return products.stream()
                .map(ProductInfo::new)
                .toList();
    }

    public String addCustomerTypeToProduct(AddCustomerTypeToProductCommand command) {
        Product product = productRepository.findById(command.getProductId())
                .orElseThrow(() -> new ProductNotExistsException(command.getProductId()));
        product.setCustomerType(command.getCustomerType());
        return "Customer type added successfully!";
    }

    public List<ProductInfo> getAllProductsOnPromotion() {
        return productRepository.findAllByPromotion()
                .orElseThrow(NoPromotionsFoundException::new)
                .stream()
                .map(ProductInfo::new)
                .toList();
    }

    public ProductInfo setProductOnPromotion(Long productId) {
        Product product = findProductById(productId);
        product.setPromotionStatus(ON_PROMOTION);
        productRepository.save(product);
        return new ProductInfo(product);
    }

    public ProductInfo setProductNotOnPromotion(Long productId) {
        Product product = findProductById(productId);
        product.setPromotionStatus(NOT_ON_PROMOTION);
        productRepository.save(product);
        return new ProductInfo(product);
    }


}
