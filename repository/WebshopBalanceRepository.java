package hu.progmasters.webshop.repository;

import hu.progmasters.webshop.domain.WebshopBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebshopBalanceRepository extends JpaRepository<WebshopBalance, Long> {
}

