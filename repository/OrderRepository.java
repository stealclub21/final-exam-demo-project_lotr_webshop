package hu.progmasters.webshop.repository;

import hu.progmasters.webshop.domain.Order;
import hu.progmasters.webshop.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT oi.product " +
           "FROM Order o " +
           "JOIN o.orderItemList oi " +
           "WHERE o.customer.id = :customerId " +
           "AND oi.product.id = :productId")
    Product findProductByCustomerIdAndProductId(Long customerId, Long productId);

    @Query("SELECT o " +
           "FROM Order o " +
           "WHERE o.customer.id = :customerId " +
           "AND o.paymentStatus = 'PENDING'")
    Order findPendingOrderByCustomerId(Long customerId);

}
