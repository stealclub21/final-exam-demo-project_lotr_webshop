package hu.progmasters.webshop.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TotalSpending {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    private Double total = 0.0;

    @OneToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    public TotalSpending(Customer customer, Double total) {
        this.customer = customer;
        this.total = total;
    }
}
