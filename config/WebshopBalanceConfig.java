package hu.progmasters.webshop.config;

import hu.progmasters.webshop.domain.WebshopBalance;
import hu.progmasters.webshop.repository.WebshopBalanceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebshopBalanceConfig {

    @Bean
    public CommandLineRunner initializeBalance(WebshopBalanceRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                WebshopBalance initialBalance = new WebshopBalance();
                repository.save(initialBalance);
            }
        };
    }
}
