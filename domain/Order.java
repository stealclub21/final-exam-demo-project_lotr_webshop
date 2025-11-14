package hu.progmasters.webshop.domain;

import hu.progmasters.webshop.domain.enumeration.OrderStatus;
import hu.progmasters.webshop.domain.enumeration.PaymentStatus;
import hu.progmasters.webshop.domain.enumeration.ShippingMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime orderDate = LocalDateTime.now();

    private Double totalPriceOfOrder;

    private String comments;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus = OrderStatus.NEW;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST)
    private List<OrderItem> orderItemList = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private ShippingMethod shippingMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
}
