package hu.progmasters.webshop.service;

import hu.progmasters.webshop.config.securityconfig.AuthUserService;
import hu.progmasters.webshop.domain.Customer;
import hu.progmasters.webshop.domain.enumeration.SubscriptionStatus;
import hu.progmasters.webshop.dto.incoming.CustomerCreateUpdateCommand;
import hu.progmasters.webshop.dto.outgoing.CustomerInfoWithPreviousOrders;
import hu.progmasters.webshop.exception.*;
import hu.progmasters.webshop.registration.token.ConfirmationTokenService;
import hu.progmasters.webshop.repository.CustomerRepository;
import hu.progmasters.webshop.repository.TotalSpendingRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;

import static hu.progmasters.webshop.domain.enumeration.CustomerType.HUMAN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TotalSpendingRepository totalSpendingRepository;

    @Mock
    private AuthUserService authUserService;

    @Mock
    private ConfirmationTokenService confirmationTokenService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CustomerService customerService;

    private String encodedPassword = "encodedPassword";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(bCryptPasswordEncoder.encode(anyString())).thenReturn(encodedPassword);
        encodedPassword = bCryptPasswordEncoder.encode("password");

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("BASIC_USER"));


        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User("correctEmail@example.com", encodedPassword, authorities);
        when(authUserService.getUserFromSession()).thenReturn(userDetails);

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setEmail("correctEmail@example.com");
        customer.setPassword(encodedPassword);
        customer.setCustomerType(HUMAN);
        customer.setFirstName("Test");
        customer.setLastName("Test");
        when(customerRepository.findByEmail("correctEmail@example.com")).thenReturn(Optional.of(customer));
    }


    @Test
    void registerCustomer_whenValidCommand_registersCustomer() {
        CustomerCreateUpdateCommand command = new CustomerCreateUpdateCommand();
        command.setPassword("testPassword");

        Customer customer = new Customer();
        customer.setPassword(command.getPassword());
        when(modelMapper.map(any(CustomerCreateUpdateCommand.class), eq(Customer.class))).thenReturn(customer);
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("encodedPassword");

        String result = customerService.registerCustomer(command);

        assertNotNull(result);
    }

    @Test
    void registerCustomer_whenDuplicateEmail_throwsException() {
        CustomerCreateUpdateCommand command = new CustomerCreateUpdateCommand();

        when(modelMapper.map(any(CustomerCreateUpdateCommand.class), eq(Customer.class))).thenReturn(new Customer());
        when(customerRepository.save(any(Customer.class))).thenThrow(new DataIntegrityViolationException(""));

        assertThrows(CustomerAlreadyExistsException.class, () -> customerService.registerCustomer(command));
    }

    @Test
    void registerCustomer_whenMissingCustomerType_throwsException() {
        CustomerCreateUpdateCommand command = new CustomerCreateUpdateCommand();

        Customer customer = new Customer();
        when(modelMapper.map(any(CustomerCreateUpdateCommand.class), eq(Customer.class))).thenReturn(customer);
        when(customerRepository.save(any(Customer.class))).thenThrow(new MissingCustomerTypeException());

        assertThrows(MissingCustomerTypeException.class, () -> customerService.registerCustomer(command));
    }

    @Test
    void getCustomerById_withInvalidCustomerId_throwsCustomerNotFoundException() {
        Long invalidCustomerId = 2L;

        when(customerRepository.findById(invalidCustomerId)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomerById(invalidCustomerId));
    }

    @Test
    void getCustomerById_whenValidId_returnsCustomerInfo() {
        Long validId = 1L;
        Customer customer = new Customer();
        customer.setEmail("correctEmail@example.com");
        customer.setPassword(encodedPassword);

        customer.setCustomerType(HUMAN);
        when(customerRepository.findById(validId)).thenReturn(Optional.of(customer));
        when(modelMapper.map(customer, CustomerInfoWithPreviousOrders.class)).thenReturn(new CustomerInfoWithPreviousOrders());

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("BASIC_USER"));

        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User("correctEmail@example.com", encodedPassword, authorities);

        assertEquals(userDetails.getUsername(), customer.getEmail());
        assertEquals(userDetails.getPassword(), customer.getPassword());
    }

    @Test
    void getCustomerById_whenInvalidId_throwsCustomerNotFoundException() {
        Long invalidId = 2L;
        when(customerRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomerById(invalidId));
    }

    @Test
    void findCustomerById_whenUnauthorizedAccess_throwsNotAuthorizedToViewThisPageException() {
        Long validId = 1L;
        Customer customer = new Customer();
        customer.setId(validId);
        customer.setEmail("correctEmail@example.com");
        customer.setPassword(encodedPassword);
        customer.setId(1L);
        customer.setFirstName("Test");
        customer.setLastName("Test");
        customer.setCustomerType(HUMAN);
        customer.setActive(true);
        customer.setEnabled(true);
        customer.setAccountNonLocked(true);

        Customer loggedInCustomer = new Customer();
        loggedInCustomer.setEmail("loggedInCustomerEmail@gmail.com");
        loggedInCustomer.setPassword(encodedPassword);
        loggedInCustomer.setId(2L);

        when(customerRepository.findById(validId)).thenReturn(Optional.of(customer));
        when(authUserService.getUserFromSession()).thenReturn(loggedInCustomer);
        when(customerRepository.findByEmail(loggedInCustomer.getEmail())).thenReturn(Optional.of(loggedInCustomer));

        assertThrows(NotAuthorizedToViewThisPageException.class, () -> customerService.findCustomerById(validId));
    }

    @Test
    void deleteCustomer_whenValidId_deactivatesCustomerSoftAndReturnsMessage() {
        Long validId = 1L;
        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("Test");
        customer.setEmail("correctEmail@example.com");
        customer.setPassword(encodedPassword);
        customer.setActive(true);
        customer.setEnabled(true);
        customer.setAccountNonLocked(true);
        customer.setCustomerType(HUMAN);

        when(customerRepository.save(customer)).thenReturn(customer);

        when(customerRepository.findById(validId)).thenReturn(Optional.of(customer));

        when(authUserService.getUserFromSession()).thenReturn(customer);

        String result = customerService.deleteCustomerSoft(validId);

        assertFalse(customer.isActive());
        assertEquals("Customer with id: " + validId + " has been deleted.", result);
    }

    @Test
    void deleteCustomer_whenInvalidId_throwsCustomerSoftNotFoundException() {
        Long invalidId = 2L;
        when(customerRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.deleteCustomerSoft(invalidId));
    }

    @Test
    void deleteCustomer_whenCustomerSoftNotLoggedIn_throwsNotAuthorizedToViewThisPageException() {
        Long validId = 1L;
        Customer customer = new Customer();
        customer.setId(validId);
        customer.setActive(true);

        when(customerRepository.findById(validId)).thenReturn(Optional.of(customer));
        when(authUserService.getUserFromSession()).thenReturn(new org.springframework.security.core.userdetails.User("wrongEmail@example.com", encodedPassword, new ArrayList<>()));

        assertThrows(UsernameNotFoundException.class, () -> customerService.deleteCustomerSoft(validId));
    }

    @Test
    void getSubscribedAndActiveEmailAddresses_whenSubscribedCustomersExist_returnsEmailAddresses() {
        List<String> expectedEmails = Arrays.asList("email1@example.com", "email2@example.com");
        when(customerRepository.findAllSubscribedAndActiveEmailAddresses()).thenReturn(Optional.of(expectedEmails));

        List<String> result = customerService.getSubscribedAndActiveEmailAddresses();

        assertEquals(expectedEmails, result);
    }

    @Test
    void getSubscribedAndActiveEmailAddresses_whenNoSubscribedCustomersExist_throwsNoSubscribedCustomersException() {
        when(customerRepository.findAllSubscribedAndActiveEmailAddresses()).thenReturn(Optional.empty());

        assertThrows(NoSubscribedCustomersException.class, () -> customerService.getSubscribedAndActiveEmailAddresses());
    }

    @Test
    void subscribeToPromotions_whenValidId_subscribesCustomer() {
        Long validId = 1L;
        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("Test");
        customer.setEmail("correctEmail@example.com");
        customer.setPassword(encodedPassword);
        customer.setActive(true);
        customer.setEnabled(true);
        customer.setAccountNonLocked(true);
        customer.setCustomerType(HUMAN);
        customer.setSubscriptionStatus(SubscriptionStatus.UNSUBSCRIBED);

        when(customerRepository.findById(validId)).thenReturn(Optional.of(customer));

        CustomerInfoWithPreviousOrders result = customerService.subscribeToPromotions(validId);

        assertEquals(SubscriptionStatus.SUBSCRIBED, customer.getSubscriptionStatus());
    }

    @Test
    void subscribeToPromotions_whenInvalidId_throwsCustomerNotFoundException() {
        Long invalidId = 2L;
        when(customerRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.subscribeToPromotions(invalidId));
    }

    @Test
    void subscribeToPromotions_whenCustomerAlreadySubscribed_doesNotChangeSubscriptionStatus() {
        Long validId = 1L;
        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("Test");
        customer.setEmail("correctEmail@example.com");
        customer.setPassword(encodedPassword);
        customer.setActive(true);
        customer.setEnabled(true);
        customer.setAccountNonLocked(true);
        customer.setCustomerType(HUMAN);
        customer.setSubscriptionStatus(SubscriptionStatus.SUBSCRIBED);

        when(customerRepository.findById(validId)).thenReturn(Optional.of(customer));

        CustomerInfoWithPreviousOrders result = customerService.subscribeToPromotions(validId);

        assertEquals(SubscriptionStatus.SUBSCRIBED, customer.getSubscriptionStatus());
    }

    @Test
    void updateCustomer_whenValidId_updatesCustomerInfo() {
        Long validId = 1L;
        String email = "correctEmail@example.com";
        String firstName = "UpdateName";
        String lastName = "UpdateNameLast";

        CustomerCreateUpdateCommand command = new CustomerCreateUpdateCommand();
        command.setFirstName(firstName);
        command.setLastName(lastName);
        command.setEmail(email);
        command.setCustomerType("HUMAN");
        command.setPassword(encodedPassword);

        Customer customer = new Customer();
        customer.setId(validId);
        customer.setFirstName("Test");
        customer.setLastName("Test");
        customer.setEmail(email);
        customer.setPassword(encodedPassword);
        customer.setCustomerType(HUMAN);
        customer.setActive(true);
        customer.setEnabled(true);
        customer.setAccountNonLocked(true);

        CustomerInfoWithPreviousOrders expectedInfo = new CustomerInfoWithPreviousOrders();
        expectedInfo.setFirstName(firstName);

        when(customerRepository.findById(validId)).thenReturn(Optional.of(customer));
        when(authUserService.getUserFromSession()).thenReturn(customer);
        when(modelMapper.map(command, Customer.class)).thenReturn(customer);
        when(modelMapper.map(customer, CustomerInfoWithPreviousOrders.class)).thenReturn(expectedInfo);

        CustomerInfoWithPreviousOrders updatedInfo = customerService.updateCustomer(command, validId);

        assertNotNull(updatedInfo);
        assertEquals("UpdateName", updatedInfo.getFirstName());
    }

    @Test
    void reactivateCustomer_whenCustomerIsActive_throwsCustomerCannotBeReactivatedException() {
        Customer customer = new Customer();
        customer.setEmail("correctEmail@example.com");
        customer.setPassword(encodedPassword);
        customer.setId(1L);
        customer.setFirstName("Test");
        customer.setLastName("Test");
        customer.setCustomerType(HUMAN);
        customer.setActive(true);
        customer.setEnabled(true);
        customer.setAccountNonLocked(true);
        customerRepository.save(customer);

        when(authUserService.getUserFromSession()).thenReturn(customer);
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));

        assertThrows(CustomerCannotBeReactivatedException.class, () -> customerService.reactivateCustomer());
    }

    @Test
    void reactivateCustomer_reactivatesInactiveCustomer() {
        Customer inactiveCustomer = new Customer();
        inactiveCustomer.setEmail("inactive@example.com");
        inactiveCustomer.setActive(false);

        when(authUserService.getUserFromSession()).thenReturn(inactiveCustomer);
        when(customerRepository.findByEmail("inactive@example.com")).thenReturn(Optional.of(inactiveCustomer));
        when(modelMapper.map(inactiveCustomer, CustomerInfoWithPreviousOrders.class)).thenReturn(new CustomerInfoWithPreviousOrders());

        CustomerInfoWithPreviousOrders result = customerService.reactivateCustomer();

        assertTrue(inactiveCustomer.isActive());
        assertNotNull(result);
    }

    @Test
    void unsubscribeFromPromotions_whenValidId_unsubscribesCustomer() {
        Long validId = 1L;
        Customer customer = new Customer();
        customer.setEmail("correctEmail@example.com");
        customer.setPassword(encodedPassword);
        customer.setId(1L);
        customer.setFirstName("Test");
        customer.setLastName("Test");
        customer.setCustomerType(HUMAN);
        customer.setActive(true);
        customer.setEnabled(true);
        customer.setAccountNonLocked(true);
        customer.setSubscriptionStatus(SubscriptionStatus.SUBSCRIBED);

        when(customerRepository.findById(validId)).thenReturn(Optional.of(customer));
        when(authUserService.getUserFromSession()).thenReturn(customer);

        customerService.unsubscribeFromPromotions(validId);

        assertEquals(SubscriptionStatus.UNSUBSCRIBED, customer.getSubscriptionStatus());
    }

    @Test
    void enableCustomer_whenCalled_enablesCustomer() {
        String email = "test@example.com";

        when(customerRepository.enableCustomer(email)).thenReturn(1);

        int result = customerService.enableCustomer(email);

        assertEquals(1, result);
    }

    @Test
    void deleteCustomerIfTokenExpired_shouldDeleteExpiredCustomers() {

        List<Long> expiredCustomerIds = Arrays.asList(1L, 2L);
        when(confirmationTokenService.getIdOfCustomersWithExpiredToken()).thenReturn(expiredCustomerIds);

        Customer customer1 = new Customer();
        customer1.setId(1L);
        Customer customer2 = new Customer();
        customer2.setId(2L);

        when(customerRepository.findById(1L)).thenReturn(java.util.Optional.of(customer1));
        when(customerRepository.findById(2L)).thenReturn(java.util.Optional.of(customer2));

        customerService.deleteCustomerIfTokenExpired();

        verify(totalSpendingRepository, times(1)).deleteByCustomerId(1L);
        verify(totalSpendingRepository, times(1)).deleteByCustomerId(2L);
        verify(customerRepository, times(1)).deleteById(1L);
        verify(customerRepository, times(1)).deleteById(2L);
        verify(confirmationTokenService, times(expiredCustomerIds.size())).deleteToken();
    }

    @Test
    void deleteCustomerIfTokenExpired_withNoExpiredCustomers_shouldNotDeleteAnyCustomer() {

        when(confirmationTokenService.getIdOfCustomersWithExpiredToken()).thenReturn(List.of());

        customerService.deleteCustomerIfTokenExpired();

        verify(totalSpendingRepository, never()).deleteByCustomerId(anyLong());
        verify(customerRepository, never()).deleteById(anyLong());
        verify(confirmationTokenService, never()).deleteToken();
    }

    @Test
    void findCustomerByEmailWithoutThrowingException_returnsCustomer_whenEmailExists() {
        String email = "existing@example.com";
        Customer expectedCustomer = new Customer();
        expectedCustomer.setEmail(email);

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(expectedCustomer));

        Customer result = customerService.findCustomerByEmailWithoutThrowingException(email);

        assertNotNull(result);
        assertEquals(expectedCustomer.getEmail(), result.getEmail());
    }

    @Test
    void findCustomerByEmailWithoutThrowingException_returnsNull_whenEmailDoesNotExist() {
        String email = "nonexistent@example.com";

        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());

        Customer result = customerService.findCustomerByEmailWithoutThrowingException(email);

        assertNull(result);
    }

    @Test
    void submitNewPassword_whenPasswordsMatch_redirectsToSuccessPage() {
        String newPassword = "newPassword";
        String confirmPassword = "newPassword";
        String email = "user@example.com";

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(new Customer()));

        RedirectView result = customerService.submitNewPassword(newPassword, confirmPassword, email);

        assertEquals("/lotr-webshop/registration/success-changing-password", result.getUrl());
    }

    @Test
    void submitNewPassword_whenPasswordsDoNotMatch_redirectsToErrorPageWithMessage() {
        String newPassword = "newPassword";
        String confirmPassword = "differentPassword";
        String email = "user@example.com";

        RedirectView result = customerService.submitNewPassword(newPassword, confirmPassword, email);

        assertTrue(Objects.requireNonNull(result.getUrl()).contains("/error-really"));
    }

    @Test
    void submitNewPassword_whenEmailNotFound_throwsUsernameNotFoundException() {
        String newPassword = "newPassword";
        String confirmPassword = "newPassword";
        String email = "nonexistent@example.com";

        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> customerService.submitNewPassword(newPassword, confirmPassword, email));
    }

    @Test
    void getCustomer_returnsCustomerInfoWithPreviousOrders_forLoggedInCustomer() {

        String userEmail = "user@example.com";
        Customer loggedInCustomer = new Customer();
        loggedInCustomer.setEmail(userEmail);
        CustomerInfoWithPreviousOrders expectedInfo = new CustomerInfoWithPreviousOrders();
        expectedInfo.setEmail(loggedInCustomer.getEmail());
        UserDetails userDetails = mock(UserDetails.class);

        when(userDetails.getUsername()).thenReturn(userEmail);
        when(authUserService.getUserFromSession()).thenReturn(userDetails);
        when(customerRepository.findByEmail(userEmail)).thenReturn(Optional.of(loggedInCustomer));
        when(modelMapper.map(loggedInCustomer, CustomerInfoWithPreviousOrders.class)).thenReturn(expectedInfo);

        CustomerInfoWithPreviousOrders result = customerService.getCustomer();

        assertNotNull(result);
        assertEquals(expectedInfo.getEmail(), result.getEmail());
    }

    @Test
    void getCustomer_throwsException_whenNoLoggedInCustomer() {
        when(authUserService.getUserFromSession()).thenThrow(new UsernameNotFoundException("User not found"));

        assertThrows(UsernameNotFoundException.class, () -> customerService.getCustomer());
    }
}