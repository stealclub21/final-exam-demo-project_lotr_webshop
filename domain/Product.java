package hu.progmasters.webshop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import hu.progmasters.webshop.domain.enumeration.CustomerType;
import hu.progmasters.webshop.domain.enumeration.ProductPromotionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private String name;

    @NonNull
    private String vendor;

    @NonNull
    private Double price;

    @NonNull
    private Integer inStock;

    @ElementCollection
    @CollectionTable(name = "tags")
    private Set<String> tags = new HashSet<>();

    @NonNull
    @Enumerated(EnumType.STRING)
    private CustomerType customerType;

    @ManyToOne
    @JoinColumn(name = "product_category_id")
    private ProductCategory productCategory;

    @OneToMany(mappedBy = "product", cascade = CascadeType.PERSIST)
    @JsonIgnore
    private List<Rating> ratings = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.PERSIST)
    private List<OrderItem> orderItemList = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.PERSIST)
    private List<Image> images = new ArrayList<>();

    @JoinTable(name = "product_promotion")
    @Enumerated(EnumType.STRING)
    private ProductPromotionStatus promotionStatus = ProductPromotionStatus.NOT_ON_PROMOTION;

    private boolean isDeleted = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(name, product.name) && Objects.equals(vendor, product.vendor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, vendor);
    }
}
