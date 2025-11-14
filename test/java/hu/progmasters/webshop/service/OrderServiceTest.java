package hu.progmasters.webshop.service;

import hu.progmasters.webshop.domain.*;
import hu.progmasters.webshop.domain.enumeration.CustomerType;
import hu.progmasters.webshop.domain.enumeration.OrderStatus;
import hu.progmasters.webshop.domain.enumeration.ShippingMethod;
import hu.progmasters.webshop.dto.incoming.AddCommentToOrder;
import hu.progmasters.webshop.dto.incoming.BasicOrderCreateUpdateCommand;
import hu.progmasters.webshop.dto.outgoing.OrderInfo;
import hu.progmasters.webshop.dto.outgoing.OrderItemInfo;
import hu.progmasters.webshop.exception.*;
import hu.progmasters.webshop.repository.OrderItemRepository;
import hu.progmasters.webshop.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private ProductService productService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private BombadilsEmporiumService bombadilsEmporiumService;

    @Mock
    private WebshopBalanceService webshopBalanceService;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrderShouldCreateNewOrderWhenNoExistingOrderItem() {
        BasicOrderCreateUpdateCommand command = new BasicOrderCreateUpdateCommand();
        command.setCustomerId(1L);
        command.setProductId(1L);
        command.setAmount(1);

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setCustomerType(CustomerType.ORC);

        Product product = new Product();
        product.setId(1L);
        product.setCustomerType(CustomerType.ORC);
        product.setInStock(10);
        product.setPrice(100.0);

        OrderItemInfo orderItemInfo = new OrderItemInfo();
        orderItemInfo.setProductId(1L);

        when(customerService.findCustomerById(command.getCustomerId())).thenReturn(customer);
        when(productService.findProductById(command.getProductId())).thenReturn(product);
        when(modelMapper.map(any(OrderItem.class), eq(OrderItemInfo.class))).thenReturn(orderItemInfo);

        orderService.createOrder(command);

        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
    }

    @Test
    void createOrderShouldThrowExceptionWhenProductNotInStock() {
        BasicOrderCreateUpdateCommand command = new BasicOrderCreateUpdateCommand();
        command.setCustomerId(1L);
        command.setProductId(1L);
        command.setAmount(11);

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setCustomerType(CustomerType.ORC);

        Product product = new Product();
        product.setId(1L);
        product.setCustomerType(CustomerType.ORC);
        product.setInStock(10);

        when(customerService.findCustomerById(command.getCustomerId())).thenReturn(customer);
        when(productService.findProductById(command.getProductId())).thenReturn(product);

        assertThrows(NotEnoughProductInWebshopException.class, () -> orderService.createOrder(command));
    }

    @Test
    void createOrderShouldThrowExceptionWhenCustomerTypeMismatch() {
        BasicOrderCreateUpdateCommand command = new BasicOrderCreateUpdateCommand();
        command.setCustomerId(1L);
        command.setProductId(1L);
        command.setAmount(1);

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setCustomerType(CustomerType.ORC);

        Product product = new Product();
        product.setId(1L);
        product.setCustomerType(CustomerType.ELF);
        product.setInStock(10);

        when(customerService.findCustomerById(command.getCustomerId())).thenReturn(customer);
        when(productService.findProductById(command.getProductId())).thenReturn(product);

        assertThrows(CustomerTypeMismatchException.class, () -> orderService.createOrder(command));
    }

    @Test
    void getOrderByIdShouldReturnOrderInfoWhenOrderExists() {
        Long orderId = 1L;
        Customer customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(customer);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.getOrderById(orderId);

        verify(orderRepository, times(1)).findById(orderId);
        verify(customerService, times(1)).compareLoggedInCustomerToCustomerToMakeOperationOn(customer);
    }

    @Test
    void getOrderByIdShouldThrowExceptionWhenOrderDoesNotExist() {
        Long orderId = 1L;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotExistsException.class, () -> orderService.getOrderById(orderId));

        verify(orderRepository, times(1)).findById(orderId);
        verify(customerService, times(0)).compareLoggedInCustomerToCustomerToMakeOperationOn(any());
    }

    @Test
    void getAllOrdersShouldReturnAllOrdersWhenOrdersExist() {
        Order order1 = new Order();
        Order order2 = new Order();
        List<Order> orders = Arrays.asList(order1, order2);

        Customer customer = new Customer();
        customer.setId(1L);

        order1.setCustomer(customer);
        order2.setCustomer(customer);

        when(orderRepository.findAll()).thenReturn(orders);

        List<OrderInfo> result = orderService.getAllOrders();

        assertEquals(2, result.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void getAllOrdersShouldReturnEmptyListWhenNoOrdersExist() {
        List<Order> orders = Collections.emptyList();

        when(orderRepository.findAll()).thenReturn(orders);

        List<OrderInfo> result = orderService.getAllOrders();

        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void deleteOrderShouldCancelOrderWhenOrderExists() {
        Long orderId = 1L;
        Customer customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(customer);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        String result = orderService.deleteOrder(orderId);

        assertEquals(String.format("Order (id: %d) has been cancelled successfully!", orderId), result);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
    }

    @Test
    void deleteOrderShouldThrowExceptionWhenOrderDoesNotExist() {
        Long orderId = 1L;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotExistsException.class, () -> orderService.deleteOrder(orderId));

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(0)).save(any(Order.class));
    }

    @Test
    void finishOrderShouldCompleteOrderWhenOrderExistsAndShippingMethodIsValid() {
        Long orderId = 1L;
        ShippingMethod method = ShippingMethod.PERSONAL_PICKUP;
        Customer customer = new Customer();
        customer.setId(1L);

        TotalSpending totalSpending = new TotalSpending();
        totalSpending.setTotal(0.0);
        customer.setTotalSpending(totalSpending);

        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setTotalPriceOfOrder(100.0);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderInfo result = orderService.finishOrder(orderId, method);

        assertEquals(OrderStatus.DONE, result.getOrderStatus());
        assertEquals(method, result.getShippingMethod());
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
        verify(bombadilsEmporiumService, times(1)).increaseTotalSpendingForCustomer(customer.getId(), 100.0);
        verify(webshopBalanceService, times(1)).addToBalance(BigDecimal.valueOf(100.0));
    }

    @Test
    void finishOrderShouldThrowExceptionWhenOrderDoesNotExist() {
        Long orderId = 1L;
        ShippingMethod method = ShippingMethod.PERSONAL_PICKUP;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotExistsException.class, () -> orderService.finishOrder(orderId, method));

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(0)).save(any(Order.class));
        verify(bombadilsEmporiumService, times(0)).increaseTotalSpendingForCustomer(anyLong(), anyDouble());
        verify(webshopBalanceService, times(0)).addToBalance(any(BigDecimal.class));
    }

    @Test
    void finishOrderShouldThrowExceptionWhenShippingMethodIsInvalid() {
        Long orderId = 1L;
        ShippingMethod method = null;

        assertThrows(ShippingMethodNotExistsException.class, () -> orderService.finishOrder(orderId, method));

        verify(orderRepository, times(0)).findById(anyLong());
        verify(orderRepository, times(0)).save(any(Order.class));
        verify(bombadilsEmporiumService, times(0)).increaseTotalSpendingForCustomer(anyLong(), anyDouble());
        verify(webshopBalanceService, times(0)).addToBalance(any(BigDecimal.class));
    }

    @Test
    void commentOrderShouldAddCommentWhenOrderExistsAndIsDone() {
        Long orderId = 1L;
        AddCommentToOrder comment = new AddCommentToOrder();
        comment.setComment("Test comment");

        Customer customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setOrderStatus(OrderStatus.DONE);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        String result = orderService.commentOrder(orderId, comment);

        assertEquals("Comment added successfully!", result);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
        assertEquals(comment.getComment(), order.getComments());
    }

    @Test
    void commentOrderShouldThrowExceptionWhenOrderDoesNotExist() {
        Long orderId = 1L;
        AddCommentToOrder comment = new AddCommentToOrder();
        comment.setComment("Test comment");

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotExistsException.class, () -> orderService.commentOrder(orderId, comment));

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(0)).save(any(Order.class));
    }

    @Test
    void commentOrderShouldThrowExceptionWhenOrderIsNotDone() {
        Long orderId = 1L;
        AddCommentToOrder comment = new AddCommentToOrder();
        comment.setComment("Test comment");

        Customer customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setOrderStatus(OrderStatus.NEW);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(OrderNotDoneException.class, () -> orderService.commentOrder(orderId, comment));

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(0)).save(any(Order.class));
    }

    @Test
    void didCustomerOrderProductShouldReturnProductWhenCustomerOrderedProduct() {
        Long customerId = 1L;
        Long productId = 1L;

        Product product = new Product();
        product.setId(productId);

        when(orderRepository.findProductByCustomerIdAndProductId(customerId, productId)).thenReturn(product);

        Product result = orderService.didCustomerOrderProduct(customerId, productId);

        assertEquals(productId, result.getId());
        verify(orderRepository, times(1)).findProductByCustomerIdAndProductId(customerId, productId);
    }

    @Test
    void didCustomerOrderProductShouldReturnNullWhenCustomerDidNotOrderProduct() {
        Long customerId = 1L;
        Long productId = 1L;

        when(orderRepository.findProductByCustomerIdAndProductId(customerId, productId)).thenReturn(null);

        Product result = orderService.didCustomerOrderProduct(customerId, productId);

        assertNull(result);
        verify(orderRepository, times(1)).findProductByCustomerIdAndProductId(customerId, productId);
    }

}