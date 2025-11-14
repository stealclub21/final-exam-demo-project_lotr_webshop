package hu.progmasters.webshop.controller;

import hu.progmasters.webshop.domain.enumeration.ShippingMethod;
import hu.progmasters.webshop.dto.incoming.AddCommentToOrder;
import hu.progmasters.webshop.dto.incoming.BasicOrderCreateUpdateCommand;
import hu.progmasters.webshop.dto.outgoing.OrderInfo;
import hu.progmasters.webshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("lotr-webshop/orders")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderInfo> createOrder(@RequestBody BasicOrderCreateUpdateCommand command) {
        return ResponseEntity.ok(orderService.createOrder(command));
    }

    @GetMapping
    public ResponseEntity<List<OrderInfo>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderInfo> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.deleteOrder(id));
    }

    @PutMapping("/{id}/done")
    public ResponseEntity<OrderInfo> finishOrder(@PathVariable Long id, @RequestBody ShippingMethod method) {
        return ResponseEntity.ok(orderService.finishOrder(id, method));
    }

    @PutMapping("/{orderId}/comment")
    public ResponseEntity<String> commentOrder(@PathVariable Long orderId, @RequestBody AddCommentToOrder comment) {
        return ResponseEntity.ok(orderService.commentOrder(orderId, comment));
    }

}
