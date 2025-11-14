package hu.progmasters.webshop.repository;

import hu.progmasters.webshop.domain.Address;
import hu.progmasters.webshop.domain.enumeration.LotrCity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("SELECT a " +
           "FROM Address a " +
           "WHERE a.zip = :zip " +
           "AND a.city = :city " +
           "AND a.street = :street " +
           "AND a.houseNumber = :houseNumber")
    Address getAddressByZipAndCityAndStreetAndHouseNumber(
            String zip, LotrCity city, String street, String houseNumber);

    @Modifying
    @Query("DELETE FROM CustomerAddress ca " +
           "WHERE ca.address.id = :addressId " +
           "AND ca.customer.id = :customerId")
    void removeCustomerAddress(Long addressId, Long customerId);

    @Query("SELECT a FROM Address a " +
           "JOIN a.customerAddressList ca " +
           "WHERE ca.customer.id = :customerId")
    List<Address> findAllByCustomerId(Long customerId);
}
