package hu.progmasters.webshop.dto.incoming;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RatingCreateCommand {

    @NotNull
    private Long productId;

    @NotNull
    @Min(value = 1, message = "Rating must be between {min} and {max}.")
    @Max(value = 5, message = "Rating must be between {min} and {max}.")
    private Integer rating;

    @NotNull
    @Size(min = 1, max = 255, message = "Comment must be between {min} and {max} characters.")
    private String comment;
}
