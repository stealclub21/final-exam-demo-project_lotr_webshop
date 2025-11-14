package hu.progmasters.webshop.repository;

import hu.progmasters.webshop.domain.TotalSpending;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TotalSpendingRepository extends JpaRepository<TotalSpending, Long>{

    @Query("SELECT t " +
            "FROM TotalSpending t " +
            "WHERE t.customer.id = :customerId")
    TotalSpending findByCustomerId(Long customerId);

    @Modifying
    @Query("DELETE FROM TotalSpending t WHERE t.customer.id = :id")
    void deleteByCustomerId(Long id);
}
