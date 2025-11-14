package hu.progmasters.webshop.service;

import hu.progmasters.webshop.domain.Customer;
import hu.progmasters.webshop.domain.Order;
import hu.progmasters.webshop.domain.OrderItem;
import hu.progmasters.webshop.domain.Product;
import hu.progmasters.webshop.domain.enumeration.OrderStatus;
import hu.progmasters.webshop.domain.enumeration.PaymentStatus;
import hu.progmasters.webshop.domain.enumeration.Role;
import hu.progmasters.webshop.domain.enumeration.ShippingMethod;
import hu.progmasters.webshop.dto.incoming.AddCommentToOrder;
import hu.progmasters.webshop.dto.incoming.BasicOrderCreateUpdateCommand;
import hu.progmasters.webshop.dto.outgoing.OrderInfo;
import hu.progmasters.webshop.dto.outgoing.OrderItemInfo;
import hu.progmasters.webshop.email.EmailService;
import hu.progmasters.webshop.exception.*;
import hu.progmasters.webshop.repository.OrderItemRepository;
import hu.progmasters.webshop.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final CustomerService customerService;
    private final ProductService productService;
    private final OrderItemRepository orderItemRepository;
    private final BombadilsEmporiumService bombadilsEmporiumService;
    private final WebshopBalanceService webshopBalanceService;
    private final EmailService emailService;

    public Product didCustomerOrderProduct(Long customerId, Long productId) {
        return orderRepository.findProductByCustomerIdAndProductId(customerId, productId);
    }

    private void checkIfCustomerIsPromotableToPremiumAndPromoteIfTrue(Customer customer) {
        if (customer.getTotalSpending().getTotal() >= 10_000
                && !customer.getRoles().contains(Role.ROLE_PREMIUM)) {
            customer.getRoles().add(Role.ROLE_PREMIUM);
            emailService.sendNotificationAboutPremiumPromotion(customer);
        }
    }

    public OrderInfo createOrder(BasicOrderCreateUpdateCommand command) {
        Customer customer = customerService.findCustomerById(command.getCustomerId());
        customerService.compareLoggedInCustomerToCustomerToMakeOperationOn(customer);
        Product product = productService.findProductById(command.getProductId());
        if (!customer.getCustomerType().equals(product.getCustomerType())) {
            throw new CustomerTypeMismatchException(customer.getCustomerType(), customer.getId(), product.getCustomerType(), product.getId());
        }

        isProductInStock(product, command.getAmount());

        Order order = getOrCreateNewOrder(customer);

        OrderItem existingOrderItem = order.getOrderItemList().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingOrderItem != null) {
            existingOrderItem.setPiecesOrdered(existingOrderItem.getPiecesOrdered() + command.getAmount());
            existingOrderItem.setTotalPrice(existingOrderItem.getTotalPrice() + product.getPrice() * command.getAmount());
            orderItemRepository.save(existingOrderItem);
        } else {
            createOrderItem(command, product, order);
        }

        double totalPrice = calculateTotalPrice(order);
        order.setTotalPriceOfOrder(totalPrice);
        orderRepository.save(order);

        return createOrderInfo(order, customer);
    }

    public OrderInfo getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotExistsException(id));
        customerService.compareLoggedInCustomerToCustomerToMakeOperationOn(order.getCustomer());
        return createOrderInfo(order, order.getCustomer());
    }

    public List<OrderInfo> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(order -> createOrderInfo(order, order.getCustomer()))
                .collect(Collectors.toList());
    }

    public String deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotExistsException(id));
        customerService.compareLoggedInCustomerToCustomerToMakeOperationOn(order.getCustomer());
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        return String.format("Order (id: %d) has been cancelled successfully!", id);
    }

    public OrderInfo finishOrder(Long id, ShippingMethod method) {
        isShippingMethodValid(method);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotExistsException(id));
        customerService.compareLoggedInCustomerToCustomerToMakeOperationOn(order.getCustomer());
        order.setOrderStatus(OrderStatus.DONE);
        order.setShippingMethod(method);

        OrderInfo orderInfo = createOrderInfo(order, order.getCustomer());

        orderRepository.save(order);

        double orderTotalPrice = order.getTotalPriceOfOrder();
        double shippingCost = shippingCostReleaseCheck(order);

        bombadilsEmporiumService
                .increaseTotalSpendingForCustomer(order.getCustomer().getId(),
                        orderTotalPrice + shippingCost);

        checkIfCustomerIsPromotableToPremiumAndPromoteIfTrue(order.getCustomer());

        webshopBalanceService
                .addToBalance(BigDecimal.valueOf(orderTotalPrice + shippingCost * 0.1));

        return orderInfo;
    }

    public String commentOrder(Long orderId, AddCommentToOrder comment) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotExistsException(orderId));
        customerService.compareLoggedInCustomerToCustomerToMakeOperationOn(order.getCustomer());

        if (order.getOrderStatus() == OrderStatus.DONE) {
            order.setComments(comment.getComment());
        } else {
            throw new OrderNotDoneException(orderId);
        }
        orderRepository.save(order);

        return "Comment added successfully!";
    }

    private double calculateTotalPrice(Order order) {
        return order.getOrderItemList().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getPiecesOrdered())
                .sum();
    }

    private void isShippingMethodValid(ShippingMethod method) {
        if (method == null || Arrays
                .stream(ShippingMethod.values())
                .noneMatch(m -> m.equals(method))) {
            throw new ShippingMethodNotExistsException(method);
        }
    }

    private double shippingCostReleaseCheck(Order order) {
        return order.getTotalPriceOfOrder() >= 150 ?
                0 :
                order.getShippingMethod().getCost();
    }

    private Order getOrCreateNewOrder(Customer customer) {
        return customer.getOrderList().stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.NEW)
                .findFirst()
                .orElseGet(() -> createNewOrder(customer));
    }

    private Order createNewOrder(Customer customer) {
        Order newOrder = new Order();
        newOrder.setOrderStatus(OrderStatus.NEW);
        newOrder.setCustomer(customer);
        newOrder.setShippingMethod(ShippingMethod.PERSONAL_PICKUP);
        customer.getOrderList().add(newOrder);
        orderRepository.save(newOrder);
        return newOrder;
    }

    private OrderItem createOrderItem(BasicOrderCreateUpdateCommand command, Product product, Order order) {
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setOrder(order);
        orderItem.setPiecesOrdered(command.getAmount());
        orderItem.setTotalPrice(product.getPrice() * command.getAmount());

        order.getOrderItemList().add(orderItem);
        product.setInStock(product.getInStock() - command.getAmount());
        return orderItemRepository.save(orderItem);
    }

    private OrderInfo createOrderInfo(Order order, Customer customer) {

        return new OrderInfo(
                order.getId(),
                order.getOrderDate(),
                order.getTotalPriceOfOrder(),
                order.getComments(),
                order.getOrderStatus(),
                customer.getId(),
                order.getShippingMethod(),
                order.getOrderItemList().stream()
                        .map(item -> {
                            OrderItemInfo orderItemInfo = modelMapper.map(item, OrderItemInfo.class);
                            orderItemInfo.setProductId(item.getProduct().getId());
                            orderItemInfo.setOrderId(item.getOrder().getId());
                            orderItemInfo.setOrderItemId(item.getId());
                            return orderItemInfo;
                        })
                        .collect(Collectors.toList())
        );
    }

    private void isProductInStock(Product product, Integer amount) {
        if (product.getInStock() - amount < 0) {
            throw new NotEnoughProductInWebshopException(product.getId());
        }
    }

    public Order findPendingOrderByCustomerId(Long customerId) {
        return orderRepository.findPendingOrderByCustomerId(customerId);
    }

    public void updatePaymentStatus(Order order, PaymentStatus paymentStatus) {
        order.setPaymentStatus(paymentStatus);
        orderRepository.save(order);
    }

}
