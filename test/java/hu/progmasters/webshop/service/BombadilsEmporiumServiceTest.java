package hu.progmasters.webshop.service;

import hu.progmasters.webshop.domain.*;
import hu.progmasters.webshop.dto.incoming.*;
import hu.progmasters.webshop.dto.outgoing.ProductInfo;
import hu.progmasters.webshop.exception.*;
import hu.progmasters.webshop.repository.BombadilsEmporiumRepository;
import hu.progmasters.webshop.repository.OrderItemRepository;
import hu.progmasters.webshop.repository.OrderRepository;
import hu.progmasters.webshop.repository.TotalSpendingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.*;

import static hu.progmasters.webshop.domain.enumeration.AddressType.BILLING;
import static hu.progmasters.webshop.domain.enumeration.CustomerType.DWARF;
import static hu.progmasters.webshop.domain.enumeration.OrderStatus.*;
import static hu.progmasters.webshop.domain.enumeration.Role.ROLE_PREMIUM;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BombadilsEmporiumServiceTest {

    @InjectMocks
    private BombadilsEmporiumService bombadilsEmporiumService;

    @Mock
    private BombadilsEmporiumRepository bombadilsEmporiumRepository;

    @Mock
    private TotalSpendingRepository totalSpendingRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @Mock
    private CustomerService customerService;

    @Mock
    private WebshopBalanceService webshopBalanceService;

    @Mock
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createProductForSale_withValidCustomerIdAndPremiumCustomer_returnsProductInfo() {
        Long customerId = 1L;
        ProductCreateUpdateCommand command = new ProductCreateUpdateCommand();
        command.setName("New Product");
        command.setPrice(100.0);
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.getRoles().add(ROLE_PREMIUM);
        ProductInfo expectedProductInfo = new ProductInfo();
        expectedProductInfo.setId(1L);
        expectedProductInfo.setName("New Product");
        Product productForEmporium = new Product();
        productForEmporium.setId(1L);

        when(customerService.findCustomerById(customerId)).thenReturn(customer);
        when(productService.createProduct(command)).thenReturn(expectedProductInfo);
        when(productService.findProductById(expectedProductInfo.getId())).thenReturn(productForEmporium);
        when(modelMapper.map(productForEmporium, ProductInfo.class)).thenReturn(expectedProductInfo);

        ProductInfo result = bombadilsEmporiumService.createProductForSale(customerId, command);

        assertNotNull(result);
        assertEquals(expectedProductInfo.getId(), result.getId());
        assertEquals(expectedProductInfo.getName(), result.getName());
    }

    @Test
    void createProductForSale_withNonPremiumCustomer_throwsException() {
        Long customerId = 2L;
        ProductCreateUpdateCommand command = new ProductCreateUpdateCommand();
        Customer customer = new Customer();
        customer.setId(customerId);

        when(customerService.findCustomerById(customerId)).thenReturn(customer);

        assertThrows(CustomerCannotAccessPremiumServicesException.class,
                     () -> bombadilsEmporiumService.createProductForSale(customerId, command));
    }

    @Test
    void createProductForSale_withInvalidCustomerId_throwsException() {
        Long invalidCustomerId = 999L;
        ProductCreateUpdateCommand command = new ProductCreateUpdateCommand();

        when(customerService.findCustomerById(invalidCustomerId)).thenThrow(CustomerNotFoundException.class);

        assertThrows(CustomerNotFoundException.class,
                     () -> bombadilsEmporiumService.createProductForSale(invalidCustomerId, command));
    }

    @Test
    void addProductToCart_withValidPremiumCustomerAndProductInStock_addsProductSuccessfully() {
        UpdateOrderItemsInCartCommand command = new UpdateOrderItemsInCartCommand();
        command.setCustomerId(1L);
        command.setEmporiumProductId(1L);
        command.setAmount(1);
        Customer premiumCustomer = new Customer();
        premiumCustomer.setId(1L);
        premiumCustomer.getRoles().add(ROLE_PREMIUM);
        Product product = new Product();
        product.setInStock(10);
        product.setPrice(100.0);
        Order existingOrder = new Order();
        BombadilsEmporium bombadilsEmporium = new BombadilsEmporium();
        bombadilsEmporium.setProduct(product);

        when(customerService.findCustomerById(1L)).thenReturn(premiumCustomer);
        when(bombadilsEmporiumRepository.findById(1L)).thenReturn(Optional.of(bombadilsEmporium));
        when(bombadilsEmporiumService.getExistingOrCreateNewOrder(premiumCustomer)).thenReturn(existingOrder);

        String result = bombadilsEmporiumService.addProductToCart(command);

        assertTrue(result.contains("added to cart"));
    }

    @Test
    void addProductToCart_withNonPremiumCustomer_throwsException() {
        UpdateOrderItemsInCartCommand command = new UpdateOrderItemsInCartCommand();
        command.setCustomerId(2L);
        command.setEmporiumProductId(1L);
        command.setAmount(1);
        Customer nonPremiumCustomer = new Customer();
        nonPremiumCustomer.setId(2L);

        when(customerService.findCustomerById(2L)).thenReturn(nonPremiumCustomer);

        assertThrows(CustomerCannotAccessPremiumServicesException.class,
                     () -> bombadilsEmporiumService.addProductToCart(command));
    }


    @Test
    void removeProductFromCart_withNonPremiumCustomer_throwsException() {
        UpdateOrderItemsInCartCommand command = new UpdateOrderItemsInCartCommand();
        command.setCustomerId(2L);
        command.setEmporiumProductId(1L);
        command.setAmount(1);
        Customer nonPremiumCustomer = new Customer();
        nonPremiumCustomer.setId(2L);

        when(customerService.findCustomerById(2L)).thenReturn(nonPremiumCustomer);

        assertThrows(CustomerCannotAccessPremiumServicesException.class, () -> bombadilsEmporiumService.removeProductFromCart(command));
    }

    @Test
    void removeProductFromCart_withProductNotInCart_throwsException() {
        UpdateOrderItemsInCartCommand command = new UpdateOrderItemsInCartCommand();
        command.setCustomerId(1L);
        command.setEmporiumProductId(2L);
        command.setAmount(1);
        Customer premiumCustomer = new Customer();
        premiumCustomer.setId(1L);
        premiumCustomer.getRoles().add(ROLE_PREMIUM);
        Product productNotInCart = new Product();
        productNotInCart.setId(2L);
        Order existingOrder = new Order();
        BombadilsEmporium bombadilsEmporium = new BombadilsEmporium();
        bombadilsEmporium.setProduct(productNotInCart);

        when(customerService.findCustomerById(1L)).thenReturn(premiumCustomer);
        when(productService.findProductById(2L)).thenReturn(productNotInCart);
        when(bombadilsEmporiumRepository.findById(2L)).thenReturn(Optional.of(bombadilsEmporium));
        when(bombadilsEmporiumService.getExistingOrCreateNewOrder(premiumCustomer)).thenReturn(existingOrder);

        assertThrows(ProductNotInCartException.class, () -> bombadilsEmporiumService.removeProductFromCart(command));
    }

    @Test
    void createNewOrderWithCustomerAndAddressId_withNonPremiumCustomer_throwsException() {
        MakeOrderCommand command = new MakeOrderCommand();
        command.setCustomerId(2L);
        Customer customer = new Customer();
        customer.setId(2L);

        when(customerService.findCustomerById(2L)).thenReturn(customer);

        assertThrows(CustomerCannotAccessPremiumServicesException.class,
                () -> bombadilsEmporiumService.createNewOrderWithCustomerAndAddressId(command));
    }

    @Test
    void createNewOrderWithCustomerAndAddressId_withNoShippingAddress_throwsException() {
        MakeOrderCommand command = new MakeOrderCommand();
        command.setCustomerId(1L);
        Customer customer = new Customer();
        customer.setId(1L);
        customer.getRoles().add(ROLE_PREMIUM);
        Address address = new Address();
        address.setAddressType(BILLING);
        CustomerAddress customerAddress = new CustomerAddress(address, customer);
        customer.getCustomerAddressList().add(customerAddress);

        when(customerService.findCustomerById(1L)).thenReturn(customer);

        assertThrows(ShippingAddressNotSetException.class,
                () -> bombadilsEmporiumService.createNewOrderWithCustomerAndAddressId(command));
    }

    @Test
    void cancelOrderWithCustomerAndAddressId_withOrderNotFound_throwsOrderNotFoundByIdException() {
        CancelReturnOrderCommand command = new CancelReturnOrderCommand();
        command.setCustomerId(1L);
        command.setOrderId(999L);

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundByIdException.class, () -> bombadilsEmporiumService.cancelOrderWithCustomerAndAddressId(command));
    }

    @Test
    void returnOrderWithCustomerAndAddressId_withValidDoneOrder_returnsOrderSuccessfully() {
        CancelReturnOrderCommand command = new CancelReturnOrderCommand();
        command.setCustomerId(1L);
        command.setOrderId(1L);
        Order order = new Order();
        order.setOrderStatus(DONE);
        Customer customer = new Customer();
        customer.setId(1L);
        TotalSpending totalSpending = new TotalSpending();
        totalSpending.setTotal(1000.0);
        customer.setTotalSpending(totalSpending);
        order.setTotalPriceOfOrder(100.0);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(customerService.findCustomerById(1L)).thenReturn(customer);
        doNothing().when(webshopBalanceService).subtractFromBalance(BigDecimal.valueOf(100.0));
        when(orderRepository.save(order)).thenReturn(order);

        String result = bombadilsEmporiumService.returnOrderWithCustomerAndAddressId(command);

        assertEquals("Order (id: 1) has been returned successfully!", result);
        assertEquals(RETURNED, order.getOrderStatus());
        assertEquals(900.0, customer.getTotalSpending().getTotal());
    }

    @Test
    void returnOrderWithCustomerAndAddressId_withOrderNotDone_throwsOrderNotDoneException() {
        CancelReturnOrderCommand command = new CancelReturnOrderCommand();
        command.setCustomerId(1L);
        command.setOrderId(1L);
        Order order = new Order();
        order.setOrderStatus(NEW);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(OrderNotDoneException.class, () -> bombadilsEmporiumService.returnOrderWithCustomerAndAddressId(command));
    }

    @Test
    void returnOrderWithCustomerAndAddressId_withOrderNotFound_throwsOrderNotFoundByIdException() {
        CancelReturnOrderCommand command = new CancelReturnOrderCommand();
        command.setCustomerId(1L);
        command.setOrderId(999L);

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundByIdException.class, () -> bombadilsEmporiumService.returnOrderWithCustomerAndAddressId(command));
    }

    @Test
    void getProductById_withValidProductId_returnsProductInfo() {
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);
        product.setName("The One Ring");
        ProductInfo expectedProductInfo = new ProductInfo();
        expectedProductInfo.setId(productId);
        expectedProductInfo.setName("The One Ring");
        BombadilsEmporium bombadilsEmporium = new BombadilsEmporium();
        bombadilsEmporium.setProduct(product);

        when(bombadilsEmporiumRepository.findById(productId)).thenReturn(Optional.of(bombadilsEmporium));
        when(modelMapper.map(product, ProductInfo.class)).thenReturn(expectedProductInfo);

        ProductInfo result = bombadilsEmporiumService.getProductById(productId);

        assertNotNull(result);
        assertEquals(expectedProductInfo.getId(), result.getId());
        assertEquals(expectedProductInfo.getName(), result.getName());
    }

    @Test
    void getProductById_withInvalidProductId_throwsProductNotFoundInBombadilsEmporiumException() {
        Long invalidProductId = 999L;

        when(bombadilsEmporiumRepository.findById(invalidProductId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundInBombadilsEmporiumException.class, () -> bombadilsEmporiumService.getProductById(invalidProductId));
    }

    @Test
    void getAllProductInfos_withNoProducts_returnsEmptyList() {
        when(bombadilsEmporiumRepository.findAllNonDeletedProducts()).thenReturn(Collections.emptyList());

        List<ProductInfo> result = bombadilsEmporiumService.getAllProductInfos();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void addCommentToOrder_withValidDoneOrder_addsCommentSuccessfully() {
        Long orderId = 1L;
        AddCommentToOrder comment = new AddCommentToOrder("Great service!");
        Order order = new Order();
        order.setOrderStatus(DONE);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        String result = bombadilsEmporiumService.addCommentToOrder(orderId, comment);

        assertEquals("Comment added successfully!", result);
        assertEquals("Great service!", order.getComments());
    }

    @Test
    void addCommentToOrder_withOrderNotDone_throwsOrderNotDoneException() {
        Long orderId = 1L;
        AddCommentToOrder comment = new AddCommentToOrder("Please refund.");

        Order order = new Order();
        order.setOrderStatus(NEW);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(OrderNotDoneException.class, () -> bombadilsEmporiumService.addCommentToOrder(orderId, comment));
    }

    @Test
    void addCommentToOrder_withOrderNotFound_throwsOrderNotFoundByIdException() {
        Long orderId = 999L;
        AddCommentToOrder comment = new AddCommentToOrder("Missing order.");

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundByIdException.class, () -> bombadilsEmporiumService.addCommentToOrder(orderId, comment));
    }

    @Test
    void addToStock_withValidCommand_updatesStockSuccessfully() {
        UpdateStockCommand command = new UpdateStockCommand();
        command.setProductId(1L);
        command.setAmount(10);

        when(productService.addToStock(command)).thenReturn("Stock updated successfully");

        String result = bombadilsEmporiumService.addToStock(command);

        assertEquals("Stock updated successfully", result);
    }

    @Test
    void addToStock_withInvalidProductId_throwsProductNotFoundException() {
        UpdateStockCommand command = new UpdateStockCommand();
        command.setProductId(999L);
        command.setAmount(10);

        when(productService.addToStock(command)).thenThrow(new ProductNotFoundInBombadilsEmporiumException(999L));

        assertThrows(ProductNotFoundInBombadilsEmporiumException.class, () -> bombadilsEmporiumService.addToStock(command));
    }

    @Test
    void removeFromStock_withValidCommand_updatesStockSuccessfully() {
        UpdateStockCommand command = new UpdateStockCommand();
        command.setProductId(1L);
        command.setAmount(5);

        when(productService.removeFromStock(command)).thenReturn("Stock updated successfully");

        String result = bombadilsEmporiumService.removeFromStock(command);

        assertEquals("Stock updated successfully", result);
    }

    @Test
    void removeFromStock_withAmountExceedingCurrentStock_throwsNotEnoughProductInBombadilsEmporiumException() {
        UpdateStockCommand command = new UpdateStockCommand();
        command.setProductId(1L);
        command.setAmount(100);

        when(productService.removeFromStock(command)).thenThrow(new NotEnoughProductInBombadilsEmporiumException(1L));

        assertThrows(NotEnoughProductInBombadilsEmporiumException.class, () -> bombadilsEmporiumService.removeFromStock(command));
    }

    @Test
    void removeFromStock_withInvalidProductId_throwsProductNotFoundException() {
        UpdateStockCommand command = new UpdateStockCommand();
        command.setProductId(999L);
        command.setAmount(5);

        when(productService.removeFromStock(command)).thenThrow(new ProductNotFoundInBombadilsEmporiumException(999L));

        assertThrows(ProductNotFoundInBombadilsEmporiumException.class, () -> bombadilsEmporiumService.removeFromStock(command));
    }

    @Test
    void calculateTotalPriceOfOrder_withMultipleOrderItems_returnsCorrectTotal() {
        Order order = new Order();
        OrderItem item1 = new OrderItem();
        item1.setTotalPrice(100.0);
        OrderItem item2 = new OrderItem();
        item2.setTotalPrice(200.0);
        order.setOrderItemList(Arrays.asList(item1, item2));

        double result = bombadilsEmporiumService.calculateTotalPriceOfOrder(order);

        assertEquals(300.0, result);
    }

    @Test
    void calculateTotalPriceOfOrder_withNoOrderItems_returnsZero() {
        Order order = new Order();
        order.setOrderItemList(new ArrayList<>());

        double result = bombadilsEmporiumService.calculateTotalPriceOfOrder(order);

        assertEquals(0.0, result);
    }

    @Test
    void calculateTotalPriceOfOrder_withNullOrderItemList_throwsNullPointerException() {
        Order order = new Order();
        order.setOrderItemList(null);

        assertThrows(NullPointerException.class, () -> bombadilsEmporiumService.calculateTotalPriceOfOrder(order));
    }

    @Test
    void increaseTotalSpendingForCustomer_updatesTotalSpendingSuccessfully() {
        Long customerId = 1L;
        double totalPrice = 100.0;
        TotalSpending totalSpending = new TotalSpending();
        totalSpending.setTotal(200.0);

        when(totalSpendingRepository.findByCustomerId(customerId)).thenReturn(totalSpending);

        bombadilsEmporiumService.increaseTotalSpendingForCustomer(customerId, totalPrice);

        assertEquals(300.0, totalSpending.getTotal());
    }

    @Test
    void increaseTotalSpendingForCustomer_withExistingCustomer_updatesTotalSpendingSuccessfully() {
        Long customerId = 1L;
        double additionalSpending = 150.0;
        TotalSpending existingTotalSpending = new TotalSpending();
        existingTotalSpending.setTotal(100.0);

        when(totalSpendingRepository.findByCustomerId(customerId)).thenReturn(existingTotalSpending);

        bombadilsEmporiumService.increaseTotalSpendingForCustomer(customerId, additionalSpending);

        assertEquals(250.0, existingTotalSpending.getTotal());
    }

    @Test
    void updateProductStock_reducesStockForSingleOrderItem() {
        Order order = new Order();
        Product product = new Product();
        product.setInStock(10);
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setPiecesOrdered(2);
        order.setOrderItemList(Collections.singletonList(orderItem));

        bombadilsEmporiumService.updateProductStock(order);

        assertEquals(8, product.getInStock());
    }

    @Test
    void updateProductStock_reducesStockForMultipleOrderItems() {
        Order order = new Order();
        Product product1 = new Product();
        product1.setInStock(10);
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setProduct(product1);
        orderItem1.setPiecesOrdered(2);

        Product product2 = new Product();
        product2.setInStock(20);
        OrderItem orderItem2 = new OrderItem();
        orderItem2.setProduct(product2);
        orderItem2.setPiecesOrdered(3);

        order.setOrderItemList(Arrays.asList(orderItem1, orderItem2));

        bombadilsEmporiumService.updateProductStock(order);

        assertEquals(8, product1.getInStock());
        assertEquals(17, product2.getInStock());
    }

    @Test
    void updateProductStock_handlesZeroPiecesOrdered() {
        Order order = new Order();
        Product product = new Product();
        product.setInStock(10);
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setPiecesOrdered(0);
        order.setOrderItemList(Collections.singletonList(orderItem));

        bombadilsEmporiumService.updateProductStock(order);

        assertEquals(10, product.getInStock());
    }

    @Test
    void updateProductStock_handlesNegativeStockCorrectly() {
        Order order = new Order();
        Product product = new Product();
        product.setInStock(5);
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setPiecesOrdered(10);
        order.setOrderItemList(Collections.singletonList(orderItem));

        bombadilsEmporiumService.updateProductStock(order);

        assertEquals(-5, product.getInStock());
    }

    @Test
    void addCustomerTypeToProduct_returnsSuccessMessage_whenCommandIsValid() {
        AddCustomerTypeToProductCommand command = new AddCustomerTypeToProductCommand();
        command.setProductId(1L);
        command.setCustomerType(DWARF);

        when(productService.addCustomerTypeToProduct(command)).thenReturn("Customer type added successfully");

        String result = bombadilsEmporiumService.addCustomerTypeToProduct(command);

        assertEquals("Customer type added successfully", result);
    }

    @Test
    void addCustomerTypeToProduct_throwsException_whenProductIdIsInvalid() {
        AddCustomerTypeToProductCommand command = new AddCustomerTypeToProductCommand();
        command.setProductId(999L);
        command.setCustomerType(DWARF);

        when(productService.addCustomerTypeToProduct(command)).thenThrow(new ProductNotFoundInBombadilsEmporiumException(999L));

        assertThrows(ProductNotFoundInBombadilsEmporiumException.class, () -> bombadilsEmporiumService.addCustomerTypeToProduct(command));
    }

    @Test
    void returnOrderIfOrderBelongsToCustomer_returnsOrderSuccessfully_whenOrderBelongsToCustomer() {
        CancelReturnOrderCommand command = new CancelReturnOrderCommand();
        command.setCustomerId(1L);
        command.setOrderId(1L);
        Order order = new Order();
        Customer customer = new Customer();
        customer.setId(1L);
        order.setCustomer(customer);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = bombadilsEmporiumService.returnOrderIfOrderBelongsToCustomer(command);

        assertNotNull(result);
        assertEquals(1L, result.getCustomer().getId());
    }

    @Test
    void returnOrderIfOrderBelongsToCustomer_throwsOrderNotFoundByIdException_whenOrderDoesNotExist() {
        CancelReturnOrderCommand command = new CancelReturnOrderCommand();
        command.setCustomerId(1L);
        command.setOrderId(999L);

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundByIdException.class, () -> bombadilsEmporiumService.returnOrderIfOrderBelongsToCustomer(command));
    }

    @Test
    void returnOrderIfOrderBelongsToCustomer_throwsOrderNotBelongToCustomerException_whenOrderDoesNotBelongToCustomer() {
        CancelReturnOrderCommand command = new CancelReturnOrderCommand();
        command.setCustomerId(1L);
        command.setOrderId(1L);
        Order order = new Order();
        Customer customer = new Customer();
        customer.setId(2L);
        order.setCustomer(customer);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(OrderNotBelongToCustomerException.class, () -> bombadilsEmporiumService.returnOrderIfOrderBelongsToCustomer(command));
    }

}
