package hu.progmasters.webshop.controller;

import hu.progmasters.webshop.dto.incoming.OrderItemCreateUpdateCommand;
import hu.progmasters.webshop.dto.outgoing.OrderItemInfo;
import hu.progmasters.webshop.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("lotr-webshop/orders/{orderId}")
@Slf4j
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderItemService orderItemService;

    @PostMapping
    public ResponseEntity<OrderItemInfo> addOrderItemToOrder(@PathVariable("orderId") Long orderId, @RequestBody OrderItemCreateUpdateCommand orderItem) {
        return ResponseEntity.ok(orderItemService.addOrderItemToOrder(orderId, orderItem));
    }

    @PutMapping("{itemId}")
    public ResponseEntity<OrderItemInfo> updateOrderItemOfOrder(@PathVariable Long orderId, @PathVariable Long itemId, @RequestBody OrderItemCreateUpdateCommand orderItem) {
        return ResponseEntity.ok(orderItemService.updateOrderItemOfOrder(orderId, itemId, orderItem));
    }

    @DeleteMapping("{itemId}")
    public ResponseEntity<String> removeOrderItemFromOrder(@PathVariable Long orderId, @PathVariable Long itemId) {
        return ResponseEntity.ok(orderItemService.removeOrderItemFromOrder(orderId, itemId));
    }

}
