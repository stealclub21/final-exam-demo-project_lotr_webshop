package hu.progmasters.webshop.repository;

import hu.progmasters.webshop.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi " +
            "FROM OrderItem oi " +
            "JOIN FETCH oi.product " +
            "WHERE oi.order.id = :orderId")
    List<OrderItem> findAllByOrderId(@Param("orderId") Long orderId);


// Not sure if this is the correct query
    @Query("SELECT oi " +
            "FROM OrderItem oi " +
            "JOIN FETCH oi.product " +
            "WHERE oi.order.customer.id = :customerId")
    List<OrderItem> findOrderItemsByCustomerId(Long customerId);
}
