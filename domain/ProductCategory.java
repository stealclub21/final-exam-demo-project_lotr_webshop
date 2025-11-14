package hu.progmasters.webshop.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product_category", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")})
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private String name;

    @OneToMany(mappedBy = "productCategory", cascade = CascadeType.PERSIST)
    private List<Product> productList = new ArrayList<>();
}
