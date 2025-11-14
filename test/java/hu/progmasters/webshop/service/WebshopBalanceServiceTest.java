package hu.progmasters.webshop.service;

import hu.progmasters.webshop.domain.WebshopBalance;
import hu.progmasters.webshop.repository.WebshopBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;

class WebshopBalanceServiceTest {

    @Mock
    private WebshopBalanceRepository webshopBalanceRepository;

    @InjectMocks
    private WebshopBalanceService webshopBalanceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addToBalance_whenValidAmount_addsToBalance() {
        BigDecimal amount = BigDecimal.valueOf(100);
        WebshopBalance balance = new WebshopBalance();
        balance.setBalance(BigDecimal.valueOf(200));

        when(webshopBalanceRepository.findById(1L)).thenReturn(Optional.of(balance));

        webshopBalanceService.addToBalance(amount);

        verify(webshopBalanceRepository, times(1)).save(balance);
    }

    @Test
    void subtractFromBalance_whenValidAmount_subtractsFromBalance() {
        BigDecimal amount = BigDecimal.valueOf(100);
        WebshopBalance balance = new WebshopBalance();
        balance.setBalance(BigDecimal.valueOf(200));

        when(webshopBalanceRepository.findById(1L)).thenReturn(Optional.of(balance));

        webshopBalanceService.subtractFromBalance(amount);

        verify(webshopBalanceRepository, times(1)).save(balance);
    }
}