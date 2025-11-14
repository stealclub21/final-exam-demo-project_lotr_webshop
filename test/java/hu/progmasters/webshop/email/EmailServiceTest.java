package hu.progmasters.webshop.email;

import hu.progmasters.webshop.domain.Customer;
import hu.progmasters.webshop.dto.outgoing.ProductInfo;
import hu.progmasters.webshop.service.CustomerService;
import hu.progmasters.webshop.service.ProductService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private CustomerService customerService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        JavaMailSenderImpl mailSenderImpl = new JavaMailSenderImpl();
        mailSenderImpl.setSession(Session.getDefaultInstance(new Properties()));
        MimeMessage mimeMessage = mailSenderImpl.createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendPromotionsToSubscribers_withValidSubscribersAndPromotions_sendsEmailsWithPromotions() {
        List<String> mockEmailAddresses = Arrays.asList("user1@example.com", "user2@example.com");
        List<ProductInfo> mockProductInfos = Arrays.asList(
                new ProductInfo(),
                new ProductInfo());
        when(customerService.getSubscribedAndActiveEmailAddresses()).thenReturn(mockEmailAddresses);
        when(productService.getAllProductsOnPromotion()).thenReturn(mockProductInfos);

        emailService.sendPromotionsToSubscribers();

        verify(mailSender, times(mockEmailAddresses.size())).send(any(MimeMessage.class));
    }

    @Test
    void sendPromotionsToSubscribers_withNoSubscribers_doesNotSendEmails() {
        when(customerService.getSubscribedAndActiveEmailAddresses()).thenReturn(Collections.emptyList());

        emailService.sendPromotionsToSubscribers();

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendPromotionsToSubscribers_withNoPromotions_sendsEmailsWithoutPromotions() {
        List<String> mockEmailAddresses = Arrays.asList("user1@example.com", "user2@example.com");
        when(customerService.getSubscribedAndActiveEmailAddresses()).thenReturn(mockEmailAddresses);
        when(productService.getAllProductsOnPromotion()).thenReturn(Collections.emptyList());

        emailService.sendPromotionsToSubscribers();

        verify(mailSender, times(mockEmailAddresses.size())).send(any(MimeMessage.class));
    }

    @Test
    void sendNotificationAboutPremiumPromotion_withValidCustomer_sendsEmail() {
        Customer mockCustomer = new Customer();
        mockCustomer.setEmail("premium@example.com");
        mockCustomer.setFirstName("John");
        mockCustomer.setLastName("Doe");

        emailService.sendNotificationAboutPremiumPromotion(mockCustomer);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void send_withValidEmailDetails_sendsEmail() {
        String to = "user@example.com";
        String emailContent = "<html><body>Test email content</body></html>";
        String subject = "subject";

        emailService.send(to, emailContent, subject);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}
