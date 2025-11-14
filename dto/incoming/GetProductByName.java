package hu.progmasters.webshop.dto.incoming;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetProductByName {

    @NotNull
    @NotBlank(message = "Must not be blank!")
    private String name;
}
