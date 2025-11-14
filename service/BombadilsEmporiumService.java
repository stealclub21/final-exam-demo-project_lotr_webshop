package hu.progmasters.webshop.service;

import hu.progmasters.webshop.domain.*;
import hu.progmasters.webshop.domain.enumeration.AddressType;
import hu.progmasters.webshop.domain.enumeration.Role;
import hu.progmasters.webshop.dto.incoming.*;
import hu.progmasters.webshop.dto.outgoing.OrderInfo2;
import hu.progmasters.webshop.dto.outgoing.OrderItemInfo2;
import hu.progmasters.webshop.dto.outgoing.ProductInfo;
import hu.progmasters.webshop.exception.*;
import hu.progmasters.webshop.repository.BombadilsEmporiumRepository;
import hu.progmasters.webshop.repository.OrderItemRepository;
import hu.progmasters.webshop.repository.OrderRepository;
import hu.progmasters.webshop.repository.TotalSpendingRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static hu.progmasters.webshop.domain.enumeration.OrderStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class BombadilsEmporiumService {
    private final BombadilsEmporiumRepository bombadilsEmporiumRepository;
    private final TotalSpendingRepository totalSpendingRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final CustomerService customerService;
    private final WebshopBalanceService webshopBalanceService;
    private final ModelMapper modelMapper;
    private static final double SERVICE_FEE = 0.15;

    public ProductInfo createProductForSale(Long customerId, ProductCreateUpdateCommand command) {
        Customer customer = customerService.findCustomerById(customerId);
        customerService.compareLoggedInCustomerToCustomerToMakeOperationOn(customer);
        checkIfCustomerIsPremiumOrAdminAndThrowExceptionIfNot(customer);

        ProductInfo productInfo = productService.createProduct(command);
        Product productForEmporium = productService.findProductById(productInfo.getId());

        addProductToBombadilsEmporium(productForEmporium, customer);
        return modelMapper.map(productForEmporium, ProductInfo.class);
    }

    public String addToStock(UpdateStockCommand command) {
        return productService.addToStock(command);
    }

    public String removeFromStock(UpdateStockCommand command) {
        return productService.removeFromStock(command);
    }

    public String addProductToCart(UpdateOrderItemsInCartCommand command) {
        Customer customer = customerService.findCustomerById(command.getCustomerId());
        customerService.compareLoggedInCustomerToCustomerToMakeOperationOn(customer);
        checkIfCustomerIsPremiumOrAdminAndThrowExceptionIfNot(customer);
        Product product = findProductById(command.getEmporiumProductId());
        Order order = getExistingOrCreateNewOrder(customer);
        isProductInStock(command, order, product);

        OrderItem orderItem = createOrderItem(product, command.getAmount(), order);
        orderItemRepository.save(orderItem);
        orderTotalPrice(order, orderItem);

        return String.format("%d %s added to cart.", command.getAmount(), product.getName());
    }

    public String removeProductFromCart(UpdateOrderItemsInCartCommand command) {
        Customer customer = customerService.findCustomerById(command.getCustomerId());
        customerService.compareLoggedInCustomerToCustomerToMakeOperationOn(customer);
        checkIfCustomerIsPremiumOrAdminAndThrowExceptionIfNot(customer);
        Product product = findProductById(command.getEmporiumProductId());
        Order order = getExistingOrCreateNewOrder(customer);
        OrderItem orderItem = getOrderItemIfInCartOrThrowException(order, product);
        int maxAmount = Math.min(orderItem.getPiecesOrdered(), command.getAmount());

        if (maxAmount == orderItem.getPiecesOrdered()) {
            order.getOrderItemList().remove(orderItem);
            orderItemRepository.delete(orderItem);
        } else {
            orderItem.setTotalPrice(orderItem.getTotalPrice() - product.getPrice() * maxAmount);
            orderItem.setPiecesOrdered(orderItem.getPiecesOrdered() - maxAmount);
        }

        order.setTotalPriceOfOrder(getCurrentTotalPrice(order));

        if (order.getOrderItemList().isEmpty()) {
            order.setOrderStatus(CANCELLED);
        }

        return String.format("%d %s removed from cart.", maxAmount, product.getName());
    }

    public OrderInfo2 createNewOrderWithCustomerAndAddressId(MakeOrderCommand command) {
        Customer customer = customerService.findCustomerById(command.getCustomerId());
        customerService.compareLoggedInCustomerToCustomerToMakeOperationOn(customer);
        checkIfCustomerIsPremiumOrAdminAndThrowExceptionIfNot(customer);
        Address address = checkIfCustomerHasShippingAddressAndThrowExceptionIfNot(customer);
        Order order = getExistingOrCreateNewOrder(customer);
        order.setShippingMethod(command.getShippingMethod());

        double totalPrice = calculateTotalPriceOfOrder(order);
        webshopBalanceService.addToBalance(BigDecimal.valueOf(totalPrice * SERVICE_FEE));
        increaseTotalSpendingForCustomer(command.getCustomerId(), totalPrice);

        updateProductStock(order);
        updateOrderStatusAndSave(order, totalPrice);
        checkOrderIsEmpty(order, command.getCustomerId());

        return createOrderItemInfo(order, address);
    }

    public String cancelOrderWithCustomerAndAddressId(CancelReturnOrderCommand command) {
        Order order = returnOrderIfOrderBelongsToCustomer(command);

        if (order.getOrderStatus() != NEW) {
            throw new OrderNotNewException(command.getOrderId());
        }
        order.setOrderStatus(CANCELLED);
        orderRepository.save(order);
        return String.format("Order (id: %d) has been cancelled successfully!", command.getOrderId());
    }

    public String returnOrderWithCustomerAndAddressId(CancelReturnOrderCommand command) {
        Order order = orderRepository.findById(command.getOrderId())
                .orElseThrow(() -> new OrderNotFoundByIdException(command.getOrderId()));

        if (order.getOrderStatus() != DONE) {
            throw new OrderNotDoneException(command.getOrderId());
        }
        order.setOrderStatus(RETURNED);

        order.getOrderItemList()
                .forEach(oi -> oi.getProduct().setInStock(oi.getProduct().getInStock() + oi.getPiecesOrdered()));

        Customer customer = customerService.findCustomerById(command.getCustomerId());
        double orderTotalPrice = order.getTotalPriceOfOrder();
        customer.getTotalSpending().setTotal(customer.getTotalSpending().getTotal() - orderTotalPrice);
        webshopBalanceService.subtractFromBalance(BigDecimal.valueOf(orderTotalPrice));

        orderRepository.save(order);

        return String.format("Order (id: %d) has been returned successfully!", command.getOrderId());
    }

    public ProductInfo getProductById(Long productId) {
        Product product = findProductById(productId);
        return modelMapper.map(product, ProductInfo.class);
    }

    public List<ProductInfo> getAllProductInfos() {
        return bombadilsEmporiumRepository.findAllNonDeletedProducts().stream()
                .map(p -> modelMapper.map(p, ProductInfo.class))
                .toList();
    }

    public String addCommentToOrder(Long orderId, AddCommentToOrder comment) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundByIdException(orderId));

        customerService.compareLoggedInCustomerToCustomerToMakeOperationOn(order.getCustomer());

        if (order.getOrderStatus() == DONE) {
            order.setComments(comment.getComment());
        } else {
            throw new OrderNotDoneException(orderId);
        }
        orderRepository.save(order);

        return "Comment added successfully!";
    }

    public String addCustomerTypeToProduct(AddCustomerTypeToProductCommand command) {
        return productService.addCustomerTypeToProduct(command);
    }

    void updateProductStock(Order order) {
        order.getOrderItemList().forEach(oi -> {
            Product product = oi.getProduct();
            product.setInStock(product.getInStock() - oi.getPiecesOrdered());
        });
    }

    void updateOrderStatusAndSave(Order order, double totalPrice) {
        order.setTotalPriceOfOrder(totalPrice);
        order.setOrderStatus(DONE);
        orderRepository.save(order);
    }

    private static void orderTotalPrice(Order order, OrderItem orderItem) {
        double totalPrice = getCurrentTotalPrice(order) == 0
                ? orderItem.getTotalPrice()
                : getCurrentTotalPrice(order);
        order.setTotalPriceOfOrder(totalPrice);
    }

    private void checkOrderIsEmpty(Order order, Long customerId) {
        if (order.getOrderItemList().isEmpty()) {
            throw new EmptyCartException(customerId);
        }
    }

    Order returnOrderIfOrderBelongsToCustomer(CancelReturnOrderCommand command) {
        Order order = orderRepository.findById(command.getOrderId())
                .orElseThrow(() -> new OrderNotFoundByIdException(command.getOrderId()));

        Customer customer = order.getCustomer();
        customerService.compareLoggedInCustomerToCustomerToMakeOperationOn(customer);

        if (!customer.getId().equals(command.getCustomerId())) {
            throw new OrderNotBelongToCustomerException(command.getCustomerId());
        }

        return order;
    }

    Address checkIfCustomerHasShippingAddressAndThrowExceptionIfNot(Customer customer) {
        if (customer.getCustomerAddressList() == null || customer.getCustomerAddressList().isEmpty()) {
            throw new CustomerHasNoAddressException(customer.getId());
        } else {
            return customer.getCustomerAddressList().stream()
                    .filter(ca -> ca.getAddress().getAddressType() == AddressType.SHIPPING)
                    .findFirst()
                    .orElseThrow(() -> new ShippingAddressNotSetException(customer.getId()))
                    .getAddress();
        }
    }

    private OrderInfo2 createOrderItemInfo(Order order, Address address) {
        OrderInfo2 orderInfo = new OrderInfo2();
        orderInfo.setCustomerName(order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName());
        orderInfo.setOrderDate(order.getOrderDate());
        orderInfo.setAddress(address);
        orderInfo.setTotalPriceOfOrder(order.getTotalPriceOfOrder());
        orderInfo.setOrderStatus(order.getOrderStatus());
        orderInfo.setOrderItemInfoList(order.getOrderItemList().stream()
                .map(oi -> new OrderItemInfo2(oi.getProduct().getId(),
                        oi.getProduct().getName(),
                        oi.getPiecesOrdered(),
                        oi.getTotalPrice()))
                .toList());
        return orderInfo;
    }

    public void increaseTotalSpendingForCustomer(Long customerId, double totalPrice) {
        TotalSpending totalSpending = totalSpendingRepository.findByCustomerId(customerId);
        totalSpending.setTotal(totalSpending.getTotal() + totalPrice);
    }

    private void isProductInStock(UpdateOrderItemsInCartCommand command, Order order, Product product) {
        int currentAmountInCart = getCurrentAmountInCart(order, product);
        int totalAmount = currentAmountInCart + command.getAmount();
        if (product.getInStock() - totalAmount < 0) {
            throw new NotEnoughProductInBombadilsEmporiumException(product.getId());
        }
    }

    private static int getCurrentAmountInCart(Order order, Product product) {
        return (int) order.getOrderItemList().stream()
                .filter(oi -> oi.getProduct().getId().equals(product.getId()))
                .mapToDouble(OrderItem::getPiecesOrdered)
                .sum();
    }

    private static OrderItem getOrderItemIfInCartOrThrowException(Order order, Product product) {
        return order.getOrderItemList().stream()
                .filter(oi -> oi.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElseThrow(() -> new ProductNotInCartException(product.getId()));
    }

    private static double getCurrentTotalPrice(Order order) {
        return order.getTotalPriceOfOrder() == null
                ? 0.0
                : order.getOrderItemList().stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
    }

    private void addProductToBombadilsEmporium(Product product, Customer customer) {
        BombadilsEmporium bombadilsEmporium = new BombadilsEmporium();
        bombadilsEmporium.setCustomer(customer);
        bombadilsEmporium.setProduct(product);
        bombadilsEmporiumRepository.save(bombadilsEmporium);
    }

    double calculateTotalPriceOfOrder(Order order) {
        return order.getOrderItemList().stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
    }

    Order getExistingOrCreateNewOrder(Customer customer) {
        return customer.getOrderList().stream()
                .filter(o -> o.getOrderStatus() == NEW)
                .findFirst()
                .orElseGet(() -> createNewOrder(customer));
    }

    OrderItem createOrderItem(Product product, Integer amount, Order order) {
        OrderItem orderItem = getOrderItemOrCreateNewOne(product, order);
        if (orderItem.getProduct() == null) {
            orderItem.setProduct(product);
            orderItem.setProduct(product);
            orderItem.setPiecesOrdered(amount);
            orderItem.setTotalPrice(product.getPrice() * amount);
            orderItem.setOrder(order);
            order.getOrderItemList().add(orderItem);
        } else {
            orderItem.setPiecesOrdered(orderItem.getPiecesOrdered() + amount);
            orderItem.setTotalPrice(orderItem.getTotalPrice() + product.getPrice() * amount);
        }
        return orderItem;
    }

    private OrderItem getOrderItemOrCreateNewOne(Product product, Order order) {
        return order.getOrderItemList().stream()
                .filter(oi -> oi.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElseGet(OrderItem::new);
    }

    private Order createNewOrder(Customer customer) {
        Order order = new Order();
        order.setCustomer(customer);
        customer.getOrderList().add(order);
        return orderRepository.save(order);
    }

    Product findProductById(Long productId) {
        return bombadilsEmporiumRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundInBombadilsEmporiumException(productId))
                .getProduct();
    }

    private void checkIfCustomerIsPremiumOrAdminAndThrowExceptionIfNot(Customer customer) {
        if (!customer.getRoles().contains(Role.ROLE_PREMIUM)
                || customer.getRoles().contains(Role.ROLE_ADMIN)) {
            throw new CustomerCannotAccessPremiumServicesException(customer.getId());
        }
    }
}