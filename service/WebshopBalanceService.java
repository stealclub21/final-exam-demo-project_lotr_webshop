package hu.progmasters.webshop.service;

import hu.progmasters.webshop.domain.WebshopBalance;
import hu.progmasters.webshop.repository.WebshopBalanceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Transactional
@RequiredArgsConstructor
public class WebshopBalanceService {
    private final WebshopBalanceRepository webshopBalanceRepository;

    public void addToBalance(BigDecimal amount) {
        WebshopBalance balance = webshopBalanceRepository.findById(1L).orElseThrow();
        balance.addToBalance(amount);
        webshopBalanceRepository.save(balance);
    }

    public void subtractFromBalance(BigDecimal amount) {
        WebshopBalance balance = webshopBalanceRepository.findById(1L).orElseThrow();
        balance.subtractFromBalance(amount);
        webshopBalanceRepository.save(balance);
    }
}
