package hu.progmasters.webshop.dto.incoming;

import hu.progmasters.webshop.domain.enumeration.CustomerType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreateUpdateCommand {

    @NotNull
    @NotBlank(message = "Must not be blank!")
    private String name;

    @NotNull
    @NotBlank(message = "Must not be blank!")
    private String vendor;

    @NotNull
    @Positive(message = "Price must be positive!")
    private Double price;

    @NotNull
    @PositiveOrZero(message = "In stock must be positive or zero!")
    private Integer inStock;

    private Set<@Size(min = 2, max = 15, message = "Tag must be between {min} and {max} characters.") String> tags = new HashSet<>();

    private String productCategory;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CustomerType customerType;

    private List<MultipartFile> images;
}
