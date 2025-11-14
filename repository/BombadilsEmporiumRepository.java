package hu.progmasters.webshop.repository;

import hu.progmasters.webshop.domain.BombadilsEmporium;
import hu.progmasters.webshop.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BombadilsEmporiumRepository extends JpaRepository<BombadilsEmporium, Long> {

    @Query("SELECT b.product " +
            "FROM BombadilsEmporium b")
    List<Product> findAllNonDeletedProducts();

    @Query("SELECT b.product " +
            "FROM BombadilsEmporium b")
    List<Product> findAllDeletedProducts();

    @Query("SELECT b " +
            "FROM BombadilsEmporium b " +
            "WHERE b.product.id = :productId")
    Optional<BombadilsEmporium> findActiveByProductId(@Param("productId") Long productId);

    @Query("SELECT CASE WHEN COUNT(b) > 0 " +
            "THEN true ELSE false END " +
            "FROM BombadilsEmporium b " +
            "WHERE b.product.id = :productId")
    boolean existsActiveByProductId(@Param("productId") Long productId);

    @Query("SELECT CASE WHEN COUNT(b) > 0 " +
            "THEN true ELSE false END " +
            "FROM BombadilsEmporium b " +
            "WHERE b.customer.id = :customerId " +
            "AND b.product.id = :productId")
    boolean existsByCustomerIdAndProductId(@Param("customerId") Long customerId,
                                           @Param("productId") Long productId);
}
