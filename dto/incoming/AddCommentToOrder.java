package hu.progmasters.webshop.dto.incoming;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddCommentToOrder {
        @NotBlank(message = "Must not be blank!")
        private String comment;
}
