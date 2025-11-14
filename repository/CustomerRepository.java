package hu.progmasters.webshop.repository;

import hu.progmasters.webshop.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c " +
            "FROM Customer c " +
            "WHERE c.email = :email")
    Optional<Customer> findByEmail(String email);

    @Query("SELECT c.email " +
            "FROM Customer c " +
            "WHERE c.subscriptionStatus = 'SUBSCRIBED' " +
            "AND c.isActive = TRUE")
    Optional<List<String>> findAllSubscribedAndActiveEmailAddresses();

    @Transactional
    @Modifying
    @Query("UPDATE Customer a " +
           "SET a.enabled = TRUE, " +
           "a.accountNonLocked = TRUE, " +
           "a.isActive = TRUE " +
           "WHERE a.email = ?1")
    int enableCustomer(String email);
}
