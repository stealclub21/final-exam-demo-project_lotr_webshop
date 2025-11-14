package hu.progmasters.webshop;

import hu.progmasters.webshop.domain.*;
import hu.progmasters.webshop.domain.enumeration.OrderStatus;
import hu.progmasters.webshop.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static hu.progmasters.webshop.domain.enumeration.AddressType.*;
import static hu.progmasters.webshop.domain.enumeration.CustomerType.*;
import static hu.progmasters.webshop.domain.enumeration.LotrCity.*;
import static hu.progmasters.webshop.domain.enumeration.ProductPromotionStatus.*;
import static hu.progmasters.webshop.domain.enumeration.Role.*;
import static hu.progmasters.webshop.domain.enumeration.ShippingMethod.PERSONAL_PICKUP;
import static hu.progmasters.webshop.domain.enumeration.SubscriptionStatus.*;

@SpringBootApplication
public class AngularWebshopApplication {

    public static void main(String[] args) {
        SpringApplication.run(AngularWebshopApplication.class, args);
    }

    @Bean
    CommandLineRunner run(CustomerRepository customerRepository,
                          AddressRepository addressRepository,
                          ProductRepository productRepository,
                          OrderRepository orderRepository,
                          OrderItemRepository orderItemRepository,
                          ProductCategoryRepository productCategoryRepository,
                          CustomerAddressRepository customerAddressRepository,
                          TotalSpendingRepository totalSpendingRepository,
                          BCryptPasswordEncoder bCryptPasswordEncoder) {
        return args -> {

            String password = bCryptPasswordEncoder.encode("111");

            ProductCategory weaponCategory = new ProductCategory();
            weaponCategory.setName("WEAPON");
            productCategoryRepository.save(weaponCategory);

            ProductCategory foodCategory = new ProductCategory();
            foodCategory.setName("FOOD");
            productCategoryRepository.save(foodCategory);

            //Admin Customer begins
            Customer customerAdmin = customerRepository
                    .save(new Customer("Admin",
                                       "Admin",
                                       "middle3arthmarket@gmail.com",
                                       password,
                                       HUMAN));
            customerAdmin.setEnabled(true);
            customerAdmin.setActive(true);
            customerAdmin.setAccountNonLocked(true);
            customerAdmin.getRoles().add(ROLE_ADMIN);
            customerRepository.save(customerAdmin);

            Address addressAdmin = addressRepository.save(new Address(BILLING, "1111", GONDOR, "Human street", "1"));
            CustomerAddress customerAddressAdmin = new CustomerAddress(addressAdmin, customerAdmin);
            customerAdmin.getCustomerAddressList().add(customerAddressAdmin);
            customerAddressRepository.save(customerAddressAdmin);

            totalSpendingRepository.save(new TotalSpending(customerAdmin, 0.0));

            //Admin Customer ends


            //Basic Customer begins
            Customer customerBasic = customerRepository.
                    save(new Customer("Legolas",
                                      "Greenleaf",
                                      "zsukk.janos@gmail.com",
                                      password,
                                      ELF));
            customerBasic.setEnabled(true);
            customerBasic.setActive(true);
            customerBasic.setAccountNonLocked(true);
            customerRepository.save(customerBasic);

            Address addressBasic = addressRepository.save(new Address(SHIPPING,"4321", RIVENDELL, "Elf street", "22"));
            CustomerAddress customerAddressBasic = new CustomerAddress(addressBasic, customerBasic);
            customerBasic.getCustomerAddressList().add(customerAddressBasic);
            customerAddressRepository.save(customerAddressBasic);

            totalSpendingRepository.save(new TotalSpending(customerBasic, 0.0));

            //Basic Customer ends


            //Premium Customer begins
            Customer customerPremium = customerRepository
                    .save(new Customer("Premium",
                                       "Premium",
                                       "cakeproject123@gmail.com",
                                       password,
                                       ORC));

            customerPremium.setEnabled(true);
            customerPremium.setActive(true);
            customerPremium.setAccountNonLocked(true);
            customerPremium.setSubscriptionStatus(SUBSCRIBED);
            customerPremium.getRoles().add(ROLE_PREMIUM);
            customerRepository.save(customerPremium);

            Address addressPremium = addressRepository.save(new Address(SHIPPING,"1234", MORDOR, "Sauron street", "66"));
            CustomerAddress customerAddressPremium = new CustomerAddress(addressPremium, customerPremium);
            customerPremium.getCustomerAddressList().add(customerAddressPremium);
            customerAddressRepository.save(customerAddressPremium);

            totalSpendingRepository.save(new TotalSpending(customerPremium, 0.0));

            //Premium Customer ends


            //BOTI-nak kell
            Product productPromotion = productRepository.save(new Product("Sword", "Moria Forge", 1000.0, 10, DWARF));
            productPromotion.setPromotionStatus(ON_PROMOTION);
            productPromotion.setProductCategory(weaponCategory);
            productRepository.save(productPromotion);

            Order orderForPremium = new Order();
            orderForPremium.setOrderStatus(OrderStatus.NEW);
            orderForPremium.setCustomer(customerPremium);
            orderForPremium.setShippingMethod(PERSONAL_PICKUP);

            OrderItem orderItemForPromotion = new OrderItem();
            orderItemForPromotion.setProduct(productPromotion);
            orderItemForPromotion.setPiecesOrdered(1);
            double totalPricePromotion = productPromotion.getPrice() * orderItemForPromotion.getPiecesOrdered();
            orderItemForPromotion.setTotalPrice(totalPricePromotion);
            orderItemForPromotion.setOrder(orderForPremium);
            orderItemRepository.save(orderItemForPromotion);

            orderForPremium.getOrderItemList().add(orderItemForPromotion);
            orderForPremium.setTotalPriceOfOrder(totalPricePromotion);
            orderRepository.save(orderForPremium);





            //Product for ELF
            Product productForElf = productRepository.save(new Product("Lembas", "Rivendell Industries", 500.0, 100, ELF));
            productPromotion.setPromotionStatus(NOT_ON_PROMOTION);
            productForElf.setProductCategory(foodCategory);
            productRepository.save(productForElf);


            //Product for HUMAN
            Product productForHuman = productRepository.save(new Product("Shield", "Gondor Corporation", 300.0, 50, HUMAN));
            productPromotion.setPromotionStatus(NOT_ON_PROMOTION);
            productForHuman.setProductCategory(weaponCategory);
            productRepository.save(productForHuman);


            //ORDER for admin
            Order orderForAdmin = new Order();
            orderForAdmin.setOrderStatus(OrderStatus.NEW);
            orderForAdmin.setCustomer(customerAdmin);
            orderForAdmin.setShippingMethod(PERSONAL_PICKUP);

            OrderItem orderItemForHuman = new OrderItem();
            orderItemForHuman.setProduct(productForHuman);
            orderItemForHuman.setPiecesOrdered(1);
            double totalPrice = productForHuman.getPrice() * orderItemForHuman.getPiecesOrdered();
            orderItemForHuman.setTotalPrice(totalPrice);
            orderItemForHuman.setOrder(orderForAdmin);
            orderItemRepository.save(orderItemForHuman);

            orderForAdmin.getOrderItemList().add(orderItemForHuman);
            orderForAdmin.setTotalPriceOfOrder(totalPrice);

            orderRepository.save(orderForAdmin);

            //Order for admin ends

        };

    }
}
