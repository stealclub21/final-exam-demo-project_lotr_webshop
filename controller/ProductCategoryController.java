package hu.progmasters.webshop.controller;

import hu.progmasters.webshop.dto.incoming.ProductCategoryCreateUpdateCommand;
import hu.progmasters.webshop.dto.outgoing.ProductCategoryInfo;
import hu.progmasters.webshop.service.ProductCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("lotr-webshop/productcategories")
@Slf4j
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @PostMapping
    @ResponseStatus(CREATED)
    public ProductCategoryInfo addProductCategory(@Valid @RequestBody ProductCategoryCreateUpdateCommand command) {
        log.info("Http request, POST / lotr-webshop / productcategories, body: {}", command.toString());
        return productCategoryService.addProductCategory(command);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(OK)
    public String deleteProductCategory(@PathVariable Long productId) {
        log.info("Http request, DELETE / lotr-webshop / productcategories / productId ");
        return productCategoryService.deleteProductCategory(productId);
    }
}
