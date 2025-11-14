package hu.progmasters.webshop.service;

import hu.progmasters.webshop.domain.Customer;
import hu.progmasters.webshop.domain.Order;
import hu.progmasters.webshop.domain.OrderItem;
import hu.progmasters.webshop.domain.Product;
import hu.progmasters.webshop.dto.incoming.OrderItemCreateUpdateCommand;
import hu.progmasters.webshop.dto.outgoing.OrderItemInfo;
import hu.progmasters.webshop.exception.*;
import hu.progmasters.webshop.repository.OrderItemRepository;
import hu.progmasters.webshop.repository.OrderRepository;
import hu.progmasters.webshop.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;
    private final CustomerService customerService;

    public OrderItemInfo addOrderItemToOrder(Long orderId, OrderItemCreateUpdateCommand orderItemCommand) {
        Order order = getOrderById(orderId);

        Customer customer = order.getCustomer();
        customerService.compareLoggedInCustomerToCustomerToMakeOperationOn(customer);

        Long productId = orderItemCommand.getProductId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotExistsException(productId));

        if (!customer.getCustomerType().equals(product.getCustomerType())) {
            throw new CustomerTypeMismatchException(customer.getCustomerType(), customer.getId(), product.getCustomerType(), product.getId());
        }

        if (product.getInStock() < orderItemCommand.getPiecesOrdered()) {
            throw new NotEnoughProductInStockException(product.getId());
        }

        OrderItem orderItem = findOrCreateOrderItem(order, product, orderItemCommand);
        orderItemRepository.save(orderItem);

        double totalPrice = calculateTotalPrice(order);
        order.setTotalPriceOfOrder(totalPrice);

        return createOrderItemInfo(orderItem);
    }

    public OrderItemInfo updateOrderItemOfOrder(Long orderId, Long itemId, OrderItemCreateUpdateCommand orderItemCommand) {
        Order order = getOrderById(orderId);

        Customer customer = order.getCustomer();
        customerService.compareLoggedInCustomerToCustomerToMakeOperationOn(customer);

        OrderItem orderItem = findOrderItemById(order, itemId);

        updateOrderItem(orderItem, orderItemCommand);

        double totalPrice = calculateTotalPrice(order);
        order.setTotalPriceOfOrder(totalPrice);

        orderRepository.save(order);

        return createOrderItemInfo(orderItem);
    }

    public String removeOrderItemFromOrder(Long orderId, Long itemId) {
        Order order = getOrderById(orderId);

        Customer customer = order.getCustomer();
        customerService.compareLoggedInCustomerToCustomerToMakeOperationOn(customer);

        OrderItem orderItem = findOrderItemById(order, itemId);
        removeOrderItem(order, orderItem);

        return "Order item (id: " + itemId + ") has been removed successfully!";
    }

    private Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotExistsException(orderId));
    }

    private OrderItem findOrCreateOrderItem(Order order, Product product, OrderItemCreateUpdateCommand orderItemCommand) {
        OrderItem orderItem = order.getOrderItemList().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (orderItem != null) {
            orderItem.setPiecesOrdered(orderItem.getPiecesOrdered() + orderItemCommand.getPiecesOrdered());
            orderItem.setTotalPrice(orderItem.getTotalPrice() + product.getPrice() * orderItemCommand.getPiecesOrdered());
        } else {
            orderItem = modelMapper.map(orderItemCommand, OrderItem.class);
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setTotalPrice(product.getPrice() * orderItemCommand.getPiecesOrdered());
            order.getOrderItemList().add(orderItem);
        }

        return orderItem;
    }

    private OrderItemInfo createOrderItemInfo(OrderItem orderItem) {
        OrderItemInfo orderItemInfo = modelMapper.map(orderItem, OrderItemInfo.class);
        orderItemInfo.setProductId(orderItem.getProduct().getId());
        orderItemInfo.setOrderId(orderItem.getOrder().getId());
        orderItemInfo.setOrderItemId(orderItem.getId());

        return orderItemInfo;
    }

    private double calculateTotalPrice(Order order) {
        return order.getOrderItemList().stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
    }

    private OrderItem findOrderItemById(Order order, Long itemId) {
        return order.getOrderItemList().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new OrderItemNotExistsException(itemId));
    }

    private void updateOrderItem(OrderItem orderItem, OrderItemCreateUpdateCommand orderItemCommand) {
        orderItem.setPiecesOrdered(orderItemCommand.getPiecesOrdered());
        orderItem.setTotalPrice(orderItem.getProduct().getPrice() * orderItemCommand.getPiecesOrdered());
    }

    private void removeOrderItem(Order order, OrderItem orderItem) {
        order.getOrderItemList().remove(orderItem);
        order.setTotalPriceOfOrder(order.getTotalPriceOfOrder() - orderItem.getTotalPrice());
        orderItemRepository.delete(orderItem);
    }

}
