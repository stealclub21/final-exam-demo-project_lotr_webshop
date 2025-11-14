package hu.progmasters.webshop.email;

import hu.progmasters.webshop.domain.Customer;
import hu.progmasters.webshop.dto.outgoing.ProductInfo;
import hu.progmasters.webshop.service.CustomerService;
import hu.progmasters.webshop.service.ProductService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessagingException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class EmailService implements EmailSender {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final CustomerService customerService;
    private final ProductService productService;

    @Scheduled(cron = "0 39 10 * * ?")
    public void sendPromotionsToSubscribers() {
        List<String> subscribedAndActiveEmailAddresses = customerService.getSubscribedAndActiveEmailAddresses();
        String promotions = productInfoFormatter(
                productService.getAllProductsOnPromotion());

        try {
            for (String email : subscribedAndActiveEmailAddresses) {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom("middle3arthmarket@gmail.com");
                helper.setTo(email);
                helper.setSubject("Promotions");
                helper.setText(
                        "<html>" +
                                "<body>" +
                                "<div style=\"text-align: center;\">\n" +
                                "    <h1>Check out our new promotions!</h1>\n" +
                                "</div>" +
                                "<br><br>"
                                + promotions +
                                "</body>" +
                                "</html>", true);
                mailSender.send(mimeMessage);
            }
        } catch (MessagingException | jakarta.mail.MessagingException e) {
            LOGGER.error("Failed to send promotion email", e);
        }
    }

    private static String productInfoFormatter(List<ProductInfo> promotions) {
        String baseUrl = "http://localhost:8080/lotr-webshop/products/";
        StringBuilder tableBuilder = new StringBuilder();

        tableBuilder.append("<table style='width:100%; border-collapse: collapse;'>")
                .append("<thead>")
                .append("<tr>")
                .append("<th style='border: 1px solid black; padding: 8px;'>Product Name</th>")
                .append("<th style='border: 1px solid black; padding: 8px;'>Vendor</th>")
                .append("<th style='border: 1px solid black; padding: 8px;'>Price</th>")
                .append("</tr>")
                .append("</thead>")
                .append("<tbody>");

        promotions.forEach(pi -> tableBuilder.append("<tr>")
                .append("<td style='border: 1px solid black; padding: 8px;'><a href='")
                .append(baseUrl)
                .append(pi.getId())
                .append("'>")
                .append(pi.getName())
                .append("</a></td>")
                .append("<td style='border: 1px solid black; padding: 8px;'>")
                .append(pi.getVendor())
                .append("</td>")
                .append("<td style='border: 1px solid black; padding: 8px;'>")
                .append(pi.getPrice())
                .append("</td>")
                .append("</tr>"));

        tableBuilder.append("</tbody>")
                .append("</table>");

        return tableBuilder.toString();
    }

    @Override
    @Async
    public void send(String to, String email, String subject) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(email, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("middle3arthmarket@gmail.com");
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            LOGGER.error("failed to send email", e);
            throw new IllegalStateException("failed to send email");
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendNotificationAboutPremiumPromotion(Customer customer) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("middle3arthmarket@gmail.com");
        message.setSubject("Promoted to premium");
        message.setText("Congratulations " +
                customer.getFirstName() +
                " " +
                customer.getLastName() +
                "! You have been promoted to premium customer!");
        message.setTo(customer.getEmail());
        mailSender.send(message);
    }
}
