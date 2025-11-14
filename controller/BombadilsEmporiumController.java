package hu.progmasters.webshop.controller;

import hu.progmasters.webshop.dto.incoming.*;
import hu.progmasters.webshop.dto.outgoing.OrderInfo2;
import hu.progmasters.webshop.dto.outgoing.ProductInfo;
import hu.progmasters.webshop.service.BombadilsEmporiumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("lotr-webshop/bombadils-emporium")
@Slf4j
@RequiredArgsConstructor
public class BombadilsEmporiumController {

    private final BombadilsEmporiumService bombadilsEmporiumService;

    @PostMapping("/create-product/{customerId}")
    @ResponseStatus(CREATED)
    public ProductInfo createProductSale(@PathVariable Long customerId,
                                         @RequestBody ProductCreateUpdateCommand command) {
        log.info("Http request, POST / lotr-webshop / bombadils-emporium / create-product, body: {}{}", customerId, command);
        return bombadilsEmporiumService.createProductForSale(customerId, command);
    }

    @PutMapping("/add-type")
    @ResponseStatus(OK)
    public String addCustomerTypeToProduct(@RequestBody AddCustomerTypeToProductCommand command) {
        log.info("Http request, PUT / lotr-webshop / bombadils-emporium / add-type, body: {}", command);
        return bombadilsEmporiumService.addCustomerTypeToProduct(command);
    }


    @PostMapping("/add-to-stock")
    @ResponseStatus(OK)
    public String addToStock(@RequestBody UpdateStockCommand command) {
        log.info("Http request, POST / lotr-webshop / bombadils-emporium / add-to-stock, body: {}", command);
        return bombadilsEmporiumService.addToStock(command);
    }

    @PostMapping("/remove-from-stock")
    @ResponseStatus(OK)
    public String removeFromStock(@RequestBody UpdateStockCommand command) {
        log.info("Http request, POST / lotr-webshop / bombadils-emporium / remove-from-stock, body: {}", command);
        return bombadilsEmporiumService.removeFromStock(command);
    }

    @PostMapping("/add-to-cart")
    @ResponseStatus(OK)
    public String addProductToCart(@RequestBody UpdateOrderItemsInCartCommand command) {
        log.info("Http request, POST / lotr-webshop / bombadils-emporium / add-to-cart, body: {}", command);
        return bombadilsEmporiumService.addProductToCart(command);
    }

    @PostMapping("/remove-from-cart")
    @ResponseStatus(OK)
    public String removeProductFromCart(@RequestBody UpdateOrderItemsInCartCommand command) {
        log.info("Http request, POST / lotr-webshop / bombadils-emporium / remove-from-cart, body: {}", command);
        return bombadilsEmporiumService.removeProductFromCart(command);
    }

    @PostMapping("/create-new-order")
    @ResponseStatus(OK)
    public OrderInfo2 createNewOrderWithCustomerAndAddressId(@RequestBody MakeOrderCommand command) {
        log.info("Http request, POST / lotr-webshop / bombadils-emporium / buy, body: {}", command);
        return bombadilsEmporiumService.createNewOrderWithCustomerAndAddressId(command);
    }

    @PostMapping("/cancel")
    @ResponseStatus(OK)
    public String cancelOrderWithCustomerAndAddressId(@RequestBody CancelReturnOrderCommand command) {
        log.info("Http request, POST / lotr-webshop / bombadils-emporium / cancel, body: {}", command);
        return bombadilsEmporiumService.cancelOrderWithCustomerAndAddressId(command);
    }

    @PostMapping("/return")
    @ResponseStatus(OK)
    public String returnOrderWithCustomerAndAddressId(@RequestBody CancelReturnOrderCommand command) {
        log.info("Http request, POST / lotr-webshop / bombadils-emporium / return, body: {}", command);
        return bombadilsEmporiumService.returnOrderWithCustomerAndAddressId(command);
    }


    @PutMapping("comment/{orderId}")
    @ResponseStatus(OK)
    public String addCommentToOrder(@PathVariable Long orderId, @RequestBody AddCommentToOrder comment) {
        log.info("Http request, PUT / lotr-webshop / bombadils-emporium / comment, body: {}{}", orderId, comment);
        return bombadilsEmporiumService.addCommentToOrder(orderId, comment);
    }

    @GetMapping("{productId}")
    @ResponseStatus(OK)
    public ProductInfo getProductById(@PathVariable Long productId) {
        log.info("Http request, GET / lotr-webshop / bombadils-emporium, body: {}", productId);
        return bombadilsEmporiumService.getProductById(productId);
    }

    @GetMapping("/all")
    @ResponseStatus(OK)
    public List<ProductInfo> getAllProductInfos() {
        log.info("Http request, GET / lotr-webshop / bombadils-emporium / all");
        return bombadilsEmporiumService.getAllProductInfos();
    }

}
