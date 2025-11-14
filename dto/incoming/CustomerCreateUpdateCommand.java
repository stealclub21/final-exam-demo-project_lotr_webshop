package hu.progmasters.webshop.dto.incoming;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerCreateUpdateCommand {

    @NotNull
    @NotBlank(message = "Must not be blank!")
    private String firstName;

    @NotNull
    @NotBlank(message = "Must not be blank!")
    private String lastName;

    @NotNull
    @NotBlank(message = "Must not be blank!")
    @Email(message = "Must be a valid email address!")
    private String email;

    @NotNull
    @NotBlank(message = "Must not be blank!")
    @Size(min = 3, max = 3, message = "Password must be exactly 3 characters long!")
    @Pattern(regexp = "\\d{3}", message = "Password must be exactly 3 digits!")
    private String password;

    @NotNull
    private String customerType;
}
