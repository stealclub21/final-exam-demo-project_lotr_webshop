package hu.progmasters.webshop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String url;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public Image(String imageUrl, String originalFilename) {
        this.url = imageUrl;
        this.name = originalFilename;
    }
}
