package hu.progmasters.webshop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "rating")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private Long customerId;

    @NonNull
    private Integer rating;

    @NonNull
    private String comment;

    private LocalDateTime ratingDate = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonIgnore
    private Product product;
}
