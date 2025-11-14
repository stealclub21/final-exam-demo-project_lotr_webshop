package hu.progmasters.webshop.registration;

import hu.progmasters.webshop.domain.Customer;
import hu.progmasters.webshop.dto.incoming.CustomerCreateUpdateCommand;
import hu.progmasters.webshop.email.EmailService;
import hu.progmasters.webshop.exception.CustomerNotFoundException;
import hu.progmasters.webshop.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("lotr-webshop/registration")
@Slf4j
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    private final CustomerService customerService;

    @PostMapping
    @ResponseStatus(CREATED)
    public String registerCustomer(@Valid @RequestBody CustomerCreateUpdateCommand command) {
        log.info("Http request, POST / lotr-webshop / customers / register, body: {}", command.toString());
        return registrationService.registerCustomer(command);
    }

    @GetMapping("/confirm")
    public RedirectView confirm(@RequestParam("token") String token) {
        registrationService.confirmToken(token);
        return new RedirectView("/custom-login");
    }

    @PostMapping("/request-reset-password")
    public RedirectView requestResetPassword(@RequestParam("email") String emailTo) {

        log.info("Http request, POST / lotr-webshop / registration / request-reset-password, email: {}", emailTo);
        Customer customer = customerService.findCustomerByEmailWithoutThrowingException(emailTo);

        if (customer == null) {
            String message = URLEncoder.encode("Username not found. Please try again.", StandardCharsets.UTF_8);
            return new RedirectView("/error-general?message=" + message);
        }

        registrationService.requestPasswordReset(emailTo, customer.getFirstName());
        return new RedirectView("/go-to-email");
    }

    @PostMapping("/submit-new-password")
    public RedirectView submitNewPassword(@RequestParam("newPassword") String newPassword,
                                    @RequestParam("confirmNewPassword") String confirmPassword,
                                    @RequestParam("email") String email) {
        log.info("Http request, POST / lotr-webshop / registration / submit-new-password, token: {}", newPassword);
        return customerService.submitNewPassword(newPassword, confirmPassword, email);
    }
}
