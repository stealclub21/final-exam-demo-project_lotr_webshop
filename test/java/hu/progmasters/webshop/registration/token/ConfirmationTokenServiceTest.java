package hu.progmasters.webshop.registration.token;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfirmationTokenServiceTest {

    @Mock
    private ConfirmationTokenRepository confirmationTokenRepository;

    @InjectMocks
    private ConfirmationTokenService confirmationTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveConfirmationToken_savesToken() {
        ConfirmationToken token = new ConfirmationToken();
        confirmationTokenService.saveConfirmationToken(token);
        verify(confirmationTokenRepository, times(1)).save(token);
    }

    @Test
    void getToken_whenTokenExists_returnsToken() {
        String tokenValue = "token";
        ConfirmationToken token = new ConfirmationToken();
        when(confirmationTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

        Optional<ConfirmationToken> result = confirmationTokenService.getToken(tokenValue);

        assertTrue(result.isPresent());
        assertEquals(token, result.get());
    }

    @Test
    void getToken_whenTokenDoesNotExist_returnsEmpty() {
        String tokenValue = "token";
        when(confirmationTokenRepository.findByToken(tokenValue)).thenReturn(Optional.empty());

        Optional<ConfirmationToken> result = confirmationTokenService.getToken(tokenValue);

        assertFalse(result.isPresent());
    }

    @Test
    void setConfirmedAt_updatesConfirmedAt() {
        String tokenValue = "token";
        when(confirmationTokenRepository.updateConfirmedAt(eq(tokenValue), any(LocalDateTime.class))).thenReturn(1);

        int result = confirmationTokenService.setConfirmedAt(tokenValue);

        assertEquals(1, result);
    }

}