package hu.progmasters.webshop.controller;

import hu.progmasters.webshop.dto.incoming.*;
import hu.progmasters.webshop.dto.incoming.GetProductByName;
import hu.progmasters.webshop.dto.incoming.ProductCreateUpdateCommand;
import hu.progmasters.webshop.dto.incoming.UploadFileCommand;
import hu.progmasters.webshop.dto.outgoing.ProductInfo;
import hu.progmasters.webshop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("lotr-webshop/products")
@Slf4j
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/admin")
    @ResponseStatus(CREATED)
    public ProductInfo createProduct(@Valid @ModelAttribute ProductCreateUpdateCommand command) {
        log.info("Http request, POST / lotr-webshop / products, body: {}", command.toString());
        return productService.createProduct(command);
    }

    @PutMapping("/admin/update/{productId}")
    @ResponseStatus(OK)
    public ProductInfo updateProduct(@Valid @RequestBody ProductCreateUpdateCommand command,
                                     @PathVariable Long productId) {
        log.info("Http request, PUT / lotr-webshop / products / update / productId, body: {}", command.toString());
        return productService.updateProduct(command, productId);
    }

    @PutMapping("/admin/{productId}/productcategory/{productCategoryId}")
    @ResponseStatus(OK)
    public ProductInfo addCategoryToProduct(@PathVariable Long productId, @PathVariable Long productCategoryId) {
        log.info("Http request, PUT / lotr-webshop / products / productId / productCategroy / productCategoryId ");
        return productService.addCategoryToProduct(productId, productCategoryId);
    }

    @DeleteMapping("/admin/delete/{productId}")
    @ResponseStatus(OK)
    public String deleteProduct(@PathVariable Long productId) {
        log.info("Http request, DELETE / lotr-webshop / products / delete / productId ");
        return productService.deleteProduct(productId);
    }

    @GetMapping
    @ResponseStatus(OK)
    public List<ProductInfo> listProducts() {
        log.info("Http request, GET / lotr-webshop / products ");
        return productService.listProducts();
    }

    @GetMapping("/{productId}")
    @ResponseStatus(OK)
    public ProductInfo getProductById(@PathVariable Long productId) {
        log.info("Http request, GET / lotr-webshop / products / productId ");
        return productService.getProductById(productId);
    }

    @GetMapping("/category/{productCategoryId}")
    @ResponseStatus(OK)
    public List<ProductInfo> getProductsByCategory(@PathVariable Long productCategoryId) {
        log.info("Http request, GET / lotr-webshop / products / productCategoryId ");
        return productService.getProductsByCategory(productCategoryId);
    }

    @GetMapping("/product_name")
    @ResponseStatus(OK)
    public List<ProductInfo> getProductsByName(@RequestBody GetProductByName command) {
        log.info("Http request, GET / lotr-webshop / products / produname, body: {}", command.toString());
        return productService.getProductsByName(command);
    }

    @PutMapping("admin/upload/{productId}")
    @ResponseStatus(OK)
    public ProductInfo uploadImages(@ModelAttribute UploadFileCommand command, @PathVariable Long productId) {
        log.info("Http request, PUT / lotr-webshop / products / upload / productId, body: {}", command.toString());
        return productService.uploadImages(command, productId);
    }

    @PutMapping("/admin/add_tags/{productId}")
    @ResponseStatus(OK)
    public ProductInfo addTags(@RequestBody TagsCreateDeleteCommand command, @PathVariable Long productId) {
        log.info("Http request, PUT / lotr-webshop / products / admin / add_tags / productId, body: {}", command.toString());
        return productService.addTags(command, productId);
    }

    @PutMapping("/admin/remove_tags/{productId}")
    @ResponseStatus(OK)
    public ProductInfo deleteTags(@RequestBody TagsCreateDeleteCommand command, @PathVariable Long productId) {
        log.info("Http request, PUT / lotr-webshop / products / admin / remove_tags / productId, body: {}", command.toString());
        return productService.deleteTags(command, productId);
    }

    @PutMapping("/admin/set_on-promotion/{productId}")
    @ResponseStatus(OK)
    public ProductInfo setProductOnPromotion(@PathVariable Long productId) {
        log.info("Http request, PUT / lotr-webshop / products / admin / set_on-promotion / productId ");
        return productService.setProductOnPromotion(productId);
    }

    @PutMapping("/admin/set_not-on-promotion/{productId}")
    @ResponseStatus(OK)
    public ProductInfo setProductNotOnPromotion(@PathVariable Long productId) {
        log.info("Http request, PUT / lotr-webshop / products / admin / set_not-on-promotion / productId ");
        return productService.setProductNotOnPromotion(productId);
    }

    @PostMapping("/add-to-stock")
    @ResponseStatus(OK)
    public String addToStock(@Valid @RequestBody UpdateStockCommand command) {
        log.info("Http request, POST / lotr-webshop / products / add-to-stock, body: {}", command.toString());
        return productService.addToStock(command);
    }

    @PostMapping("/remove-from-stock")
    @ResponseStatus(OK)
    public String removeFromStock(@Valid @RequestBody UpdateStockCommand command) {
        log.info("Http request, POST / lotr-webshop / products / remove-from-stock, body: {}", command.toString());
        return productService.removeFromStock(command);
    }

}
