package hu.progmasters.webshop.service;

import hu.progmasters.webshop.config.securityconfig.AuthUserService;
import hu.progmasters.webshop.domain.Customer;
import hu.progmasters.webshop.domain.TotalSpending;
import hu.progmasters.webshop.domain.enumeration.SubscriptionStatus;
import hu.progmasters.webshop.dto.incoming.CustomerCreateUpdateCommand;
import hu.progmasters.webshop.dto.outgoing.CustomerInfoWithPreviousOrders;
import hu.progmasters.webshop.exception.*;
import hu.progmasters.webshop.registration.token.ConfirmationToken;
import hu.progmasters.webshop.registration.token.ConfirmationTokenService;
import hu.progmasters.webshop.repository.CustomerRepository;
import hu.progmasters.webshop.repository.TotalSpendingRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.RedirectView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
@Transactional
@RequiredArgsConstructor
@EnableScheduling
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final TotalSpendingRepository totalSpendingRepository;
    private final AuthUserService authUserService;
    private final ConfirmationTokenService confirmationTokenService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ModelMapper modelMapper;
    private final EntityManager entityManager;


    public String registerCustomer(CustomerCreateUpdateCommand command) {
        Customer customer;
        ConfirmationToken confirmationToken;
        try {
            customer = modelMapper.map(command, Customer.class);
            customerRepository.save(customer);

            creatingTotalSpendingForNewCustomer(customer);

            customer.setPassword(bCryptPasswordEncoder.encode(command.getPassword()));

            confirmationToken = generateToken(customer);

        } catch (DataIntegrityViolationException e) {
            throw new CustomerAlreadyExistsException(command.getEmail());
        } catch (MappingException e) {
            throw new MissingCustomerTypeException();
        }
        return confirmationToken.getToken();
    }

    public RedirectView submitNewPassword(String newPassword, String confirmPassword, String email) {
        if (newPassword.equals(confirmPassword)) {
            Customer customer = findCustomerByEmail(email);
            customer.setPassword(bCryptPasswordEncoder.encode(newPassword));

            return new RedirectView("/lotr-webshop/registration/success-changing-password");
        } else {
            return new RedirectView("/error-really");
        }
    }

    private void creatingTotalSpendingForNewCustomer(Customer customer) {
        TotalSpending totalSpending = new TotalSpending();
        totalSpending.setCustomer(customer);
        totalSpendingRepository.save(totalSpending);
    }

    public CustomerInfoWithPreviousOrders getCustomerById(Long customerId) {

        Customer customer = findCustomerById(customerId);
        compareLoggedInCustomerToCustomerToMakeOperationOn(customer);
        return modelMapper.map(customer, CustomerInfoWithPreviousOrders.class);
    }

    public CustomerInfoWithPreviousOrders getCustomer() {
        Customer customer = getLoggedInCustomer();
        return modelMapper.map(customer, CustomerInfoWithPreviousOrders.class);
    }

    public CustomerInfoWithPreviousOrders updateCustomer(CustomerCreateUpdateCommand command, Long customerId) {

        Customer customer = findCustomerById(customerId);
        compareLoggedInCustomerToCustomerToMakeOperationOn(customer);

        String encodedPassword = bCryptPasswordEncoder.encode(command.getPassword());

        modelMapper.map(command, customer);
        customer.setPassword(encodedPassword);

        return modelMapper.map(customer, CustomerInfoWithPreviousOrders.class);
    }

    public String deleteCustomerSoft(Long customerId) {

        Customer customer = findCustomerById(customerId);
        compareLoggedInCustomerToCustomerToMakeOperationOn(customer);
        customer.setActive(false);
        return "Customer with id: " + customerId + " has been deleted.";
    }

    public CustomerInfoWithPreviousOrders reactivateCustomer() {

        Customer customer = getLoggedInCustomer();
        if (customer.isActive()) {
            throw new CustomerCannotBeReactivatedException(customer.getEmail());
        } else {
            customer.setActive(true);
            return modelMapper.map(customer, CustomerInfoWithPreviousOrders.class);
        }
    }

    public Customer findCustomerById(Long customerId) {

        Customer customer = customerRepository.findById(customerId)
                                              .orElseThrow(() -> new CustomerNotFoundException(customerId.toString()));

        compareLoggedInCustomerToCustomerToMakeOperationOn(customer);
        return customer;
    }

    private Customer findCustomerByIdWithoutAuthentication(Long customerId) {

        return customerRepository.findById(customerId)
                                              .orElseThrow(() -> new CustomerNotFoundException(customerId.toString()));
    }

    public List<String> getSubscribedAndActiveEmailAddresses() {
        return customerRepository.findAllSubscribedAndActiveEmailAddresses()
                                 .orElseThrow(NoSubscribedCustomersException::new);
    }

    public CustomerInfoWithPreviousOrders subscribeToPromotions(Long customerId) {
        Customer customer = findCustomerById(customerId);
        compareLoggedInCustomerToCustomerToMakeOperationOn(customer);
        customer.setSubscriptionStatus(SubscriptionStatus.SUBSCRIBED);
        return modelMapper.map(customer, CustomerInfoWithPreviousOrders.class);
    }

    public int enableCustomer(String email) {
        return customerRepository.enableCustomer(email);
    }

    private ConfirmationToken generateToken(Customer customer) {
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(token,
                                                                    LocalDateTime.now(),
                                                                    LocalDateTime.now().plusMinutes(60),
                                                                    customer);

        confirmationTokenService.saveConfirmationToken(confirmationToken);
        return confirmationToken;
    }

    public Customer getLoggedInCustomer() {
        UserDetails userDetails = authUserService.getUserFromSession();
        return findCustomerByEmail(userDetails.getUsername());
    }

    public Customer findCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                                 .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public Customer findCustomerByEmailWithoutThrowingException(String email) {
        return customerRepository.findByEmail(email)
                                    .orElse(null);
    }

    public void compareLoggedInCustomerToCustomerToMakeOperationOn(Customer customer) {
        Customer loggedIncustomer = getLoggedInCustomer();

        if (!customer.equals(loggedIncustomer)) {
            throw new NotAuthorizedToViewThisPageException();
        }
    }

    public CustomerInfoWithPreviousOrders unsubscribeFromPromotions(Long customerId) {
        Customer customer = findCustomerById(customerId);
        compareLoggedInCustomerToCustomerToMakeOperationOn(customer);
        customer.setSubscriptionStatus(SubscriptionStatus.UNSUBSCRIBED);
        return modelMapper.map(customer, CustomerInfoWithPreviousOrders.class);
    }

    @Scheduled(cron = "0 00 00 * * ?")
    public void deleteCustomerIfTokenExpired() {

        List<Long> idsOfCustomersWithExpiredTokens = confirmationTokenService.getIdOfCustomersWithExpiredToken();

        idsOfCustomersWithExpiredTokens.forEach(id -> {
            totalSpendingRepository.deleteByCustomerId(id);
            Customer customer = findCustomerByIdWithoutAuthentication(id);
            customer.getRoles().clear();
            entityManager.flush();
            entityManager.clear();
            customerRepository.save(customer);
            confirmationTokenService.deleteToken();
            customerRepository.deleteById(id);
        });
    }
}
