package hu.progmasters.webshop.registration;

import hu.progmasters.webshop.dto.incoming.CustomerCreateUpdateCommand;
import hu.progmasters.webshop.email.EmailSender;
import hu.progmasters.webshop.exception.TokenAlreadyConfirmedException;
import hu.progmasters.webshop.exception.TokenExpiredException;
import hu.progmasters.webshop.exception.TokenNotFoundException;
import hu.progmasters.webshop.registration.token.ConfirmationToken;
import hu.progmasters.webshop.registration.token.ConfirmationTokenService;
import hu.progmasters.webshop.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RegistrationServiceTest {


    @Mock
    private CustomerService customerService;

    @Mock
    private EmailSender emailSender;

    @Mock
    private ConfirmationTokenService confirmationTokenService;

    private RegistrationService registrationService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        registrationService = new RegistrationService(customerService, emailSender, confirmationTokenService);
    }

    @Test
    public void registerCustomer_whenValidCommand_sendsEmail() {
        CustomerCreateUpdateCommand command = new CustomerCreateUpdateCommand();
        command.setEmail("test@test.com");
        command.setFirstName("Test");

        when(customerService.registerCustomer(command)).thenReturn("token");

        registrationService.registerCustomer(command);

        verify(emailSender, times(1)).send(anyString(), anyString(), anyString());
    }

    @Test
    public void confirmToken_whenTokenNotFound_throwsException() {
        when(confirmationTokenService.getToken(anyString())).thenReturn(Optional.empty());

        assertThrows(TokenNotFoundException.class, () -> registrationService.confirmToken("token"));
    }

    @Test
    public void confirmToken_whenTokenAlreadyConfirmed_throwsException() {
        ConfirmationToken token = new ConfirmationToken();
        token.setConfirmedAt(LocalDateTime.now());

        when(confirmationTokenService.getToken(anyString())).thenReturn(Optional.of(token));

        assertThrows(TokenAlreadyConfirmedException.class, () -> registrationService.confirmToken("token"));
    }

    @Test
    public void confirmToken_whenTokenExpired_throwsException() {
        ConfirmationToken token = new ConfirmationToken();
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(confirmationTokenService.getToken(anyString())).thenReturn(Optional.of(token));

        assertThrows(TokenExpiredException.class, () -> registrationService.confirmToken("token"));
    }

    @Test
    public void confirmToken_whenValidToken_enablesCustomer() {
        ConfirmationToken token = new ConfirmationToken();
    }

    @Test
    public void requestPasswordReset_sendsEmailWithCorrectLink() {
        String emailTo = "user@example.com";
        String firstName = "User";
        String expectedLink = "http://localhost:8080/lotr-webshop/registration/reset-password?email=" + emailTo;

        registrationService.requestPasswordReset(emailTo, firstName);

        verify(emailSender, times(1)).send(
                eq(emailTo),
                contains(expectedLink),
                eq("Change your password"));
    }

    @Test
    public void requestPasswordReset_sendsEmailWithPersonalizedMessage() {
        String emailTo = "user@example.com";
        String firstName = "User";
        String expectedMessageStart = "Hi User,";

        registrationService.requestPasswordReset(emailTo, firstName);

        verify(emailSender, times(1)).send(
                eq(emailTo),
                argThat(argument -> argument.contains(expectedMessageStart)),
                eq("Change your password"));
    }

    @Test
    public void requestPasswordReset_withNullEmail_doesNotSendEmail() {
        String emailTo = null;
        String firstName = "User";

        registrationService.requestPasswordReset(emailTo, firstName);

        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }
}