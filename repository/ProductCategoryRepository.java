package hu.progmasters.webshop.repository;

import hu.progmasters.webshop.domain.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    @Query("SELECT pc FROM ProductCategory pc WHERE pc.name = :name")
    ProductCategory findByName(String name);
}
