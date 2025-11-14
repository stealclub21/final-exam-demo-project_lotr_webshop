package hu.progmasters.webshop.service;

import hu.progmasters.webshop.domain.Customer;
import hu.progmasters.webshop.domain.Order;
import hu.progmasters.webshop.domain.OrderItem;
import hu.progmasters.webshop.domain.Product;
import hu.progmasters.webshop.domain.enumeration.CustomerType;
import hu.progmasters.webshop.dto.incoming.OrderItemCreateUpdateCommand;
import hu.progmasters.webshop.dto.outgoing.OrderItemInfo;
import hu.progmasters.webshop.exception.*;
import hu.progmasters.webshop.repository.OrderItemRepository;
import hu.progmasters.webshop.repository.OrderRepository;
import hu.progmasters.webshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private OrderItemService orderItemService;

    @Mock
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        modelMapper = new ModelMapper();
        orderItemService = new OrderItemService(orderItemRepository, orderRepository, modelMapper, productRepository, customerService);
    }

    @Test
    void addOrderItemToOrderShouldAddItemWhenOrderAndProductExistAndCustomerTypesMatch() {
        Long orderId = 1L;
        OrderItemCreateUpdateCommand command = new OrderItemCreateUpdateCommand();
        command.setProductId(1L);
        command.setPiecesOrdered(1);

        Order order = new Order();
        order.setId(orderId);
        Customer customer = new Customer();
        customer.setCustomerType(CustomerType.ORC);
        order.setCustomer(customer);

        Product product = new Product();
        product.setId(1L);
        product.setCustomerType(CustomerType.ORC);
        product.setInStock(2);
        product.setPrice(1000.0);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findById(command.getProductId())).thenReturn(Optional.of(product));

        OrderItemInfo result = orderItemService.addOrderItemToOrder(orderId, command);

        assertNotNull(result);
        verify(orderRepository, times(1)).findById(orderId);
        verify(productRepository, times(1)).findById(command.getProductId());
        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
    }

    @Test
    void addOrderItemToOrderShouldThrowExceptionWhenProductDoesNotExist() {
        Long orderId = 1L;
        OrderItemCreateUpdateCommand command = new OrderItemCreateUpdateCommand();
        command.setProductId(1L);
        command.setPiecesOrdered(1);

        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(new Customer());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findById(command.getProductId())).thenReturn(Optional.empty());

        assertThrows(ProductNotExistsException.class, () -> orderItemService.addOrderItemToOrder(orderId, command));
    }

    @Test
    void addOrderItemToOrderShouldThrowExceptionWhenCustomerTypeMismatch() {
        Long orderId = 1L;
        OrderItemCreateUpdateCommand command = new OrderItemCreateUpdateCommand();
        command.setProductId(1L);
        command.setPiecesOrdered(1);

        Order order = new Order();
        order.setId(orderId);
        Customer customer = new Customer();
        customer.setCustomerType(CustomerType.ORC);
        order.setCustomer(customer);

        Product product = new Product();
        product.setId(1L);
        product.setCustomerType(CustomerType.ELF);
        product.setInStock(2);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findById(command.getProductId())).thenReturn(Optional.of(product));

        assertThrows(CustomerTypeMismatchException.class, () -> orderItemService.addOrderItemToOrder(orderId, command));
    }

    @Test
    void addOrderItemToOrderShouldThrowExceptionWhenNotEnoughProductInStock() {
        Long orderId = 1L;
        OrderItemCreateUpdateCommand command = new OrderItemCreateUpdateCommand();
        command.setProductId(1L);
        command.setPiecesOrdered(2);

        Order order = new Order();
        order.setId(orderId);
        Customer customer = new Customer();
        customer.setCustomerType(CustomerType.ORC);
        order.setCustomer(customer);

        Product product = new Product();
        product.setId(1L);
        product.setCustomerType(order.getCustomer().getCustomerType());
        product.setInStock(1);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findById(command.getProductId())).thenReturn(Optional.of(product));

        assertThrows(NotEnoughProductInStockException.class, () -> orderItemService.addOrderItemToOrder(orderId, command));
    }

    @Test
    void updateOrderItemOfOrderShouldUpdateItemWhenOrderAndItemExist() {
        Long orderId = 1L;
        Long itemId = 1L;
        OrderItemCreateUpdateCommand command = new OrderItemCreateUpdateCommand();
        command.setProductId(1L);
        command.setPiecesOrdered(2);

        Order order = new Order();
        order.setId(orderId);
        Customer customer = new Customer();
        customer.setCustomerType(CustomerType.ORC);
        order.setCustomer(customer);

        Product product = new Product();
        product.setId(1L);
        product.setPrice(1000.0);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(itemId);
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setPiecesOrdered(1);
        orderItem.setTotalPrice(1000.0);

        order.getOrderItemList().add(orderItem);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderItemInfo result = orderItemService.updateOrderItemOfOrder(orderId, itemId, command);

        assertNotNull(result);
        assertEquals(2, result.getPiecesOrdered());
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void updateOrderItemOfOrderShouldThrowExceptionWhenOrderDoesNotExist() {
        Long orderId = 1L;
        Long itemId = 1L;
        OrderItemCreateUpdateCommand command = new OrderItemCreateUpdateCommand();
        command.setProductId(1L);
        command.setPiecesOrdered(2);

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotExistsException.class, () -> orderItemService.updateOrderItemOfOrder(orderId, itemId, command));
    }

    @Test
    void updateOrderItemOfOrderShouldThrowExceptionWhenOrderItemDoesNotExist() {
        Long orderId = 1L;
        Long itemId = 1L;
        OrderItemCreateUpdateCommand command = new OrderItemCreateUpdateCommand();
        command.setProductId(1L);
        command.setPiecesOrdered(2);

        Order order = new Order();
        order.setId(orderId);
        Customer customer = new Customer();
        customer.setCustomerType(CustomerType.ORC);
        order.setCustomer(customer);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(OrderItemNotExistsException.class, () -> orderItemService.updateOrderItemOfOrder(orderId, itemId, command));
    }

    @Test
    void removeOrderItemFromOrderShouldRemoveItemWhenOrderAndItemExist() {
        Long orderId = 1L;
        Long itemId = 1L;

        Order order = new Order();
        order.setId(orderId);
        order.setTotalPriceOfOrder(1000.0);
        Customer customer = new Customer();
        customer.setCustomerType(CustomerType.ORC);
        order.setCustomer(customer);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(itemId);
        orderItem.setTotalPrice(1000.0);
        orderItem.setOrder(order);

        order.getOrderItemList().add(orderItem);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        String result = orderItemService.removeOrderItemFromOrder(orderId, itemId);

        assertEquals("Order item (id: " + itemId + ") has been removed successfully!", result);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderItemRepository, times(1)).delete(orderItem);
    }

    @Test
    void removeOrderItemFromOrderShouldThrowExceptionWhenOrderDoesNotExist() {
        Long orderId = 1L;
        Long itemId = 1L;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotExistsException.class, () -> orderItemService.removeOrderItemFromOrder(orderId, itemId));
    }

    @Test
    void removeOrderItemFromOrderShouldThrowExceptionWhenOrderItemDoesNotExist() {
        Long orderId = 1L;
        Long itemId = 1L;

        Order order = new Order();
        order.setId(orderId);
        Customer customer = new Customer();
        customer.setCustomerType(CustomerType.ORC);
        order.setCustomer(customer);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(OrderItemNotExistsException.class, () -> orderItemService.removeOrderItemFromOrder(orderId, itemId));
    }

}