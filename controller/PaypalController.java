package hu.progmasters.webshop.controller;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import hu.progmasters.webshop.domain.Order;
import hu.progmasters.webshop.service.CustomerService;
import hu.progmasters.webshop.service.OrderService;
import hu.progmasters.webshop.service.PaypalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import static hu.progmasters.webshop.domain.enumeration.PaymentStatus.CANCELLED;
import static hu.progmasters.webshop.domain.enumeration.PaymentStatus.COMPLETED;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PaypalController {

    private final PaypalService paypalService;
    private final CustomerService customerService;
    private final OrderService orderService;

    @GetMapping("/checkout")
    public String showCheckoutPage(Model model) {
        double totalPrice = getPendingOrderByCustomerId().getTotalPriceOfOrder();
        if (totalPrice > 0) {
            model.addAttribute("totalPrice", totalPrice);
        }
        return "checkout";
    }

    @PostMapping("/payment/create")
    public RedirectView createPayment(@RequestParam("method") String method,
                                      @RequestParam("currency") String currency,
                                      @RequestParam("description") String description,
                                      @RequestParam("amount") double amount) {
        Order order = getPendingOrderByCustomerId();
        double totalPrice = order.getTotalPriceOfOrder();
        if (totalPrice > 0) {
            try {
                Payment payment = paypalService.createPayment(
                        amount, currency, method, "sale", description,
                        "http://localhost:8080/payment/cancel",
                        "http://localhost:8080/payment/success");
                return new RedirectView(payment.getLinks().stream()
                        .filter(link -> "approval_url".equals(link.getRel()))
                        .findFirst()
                        .map(Links::getHref)
                        .orElse("/payment/error"));
            } catch (PayPalRESTException e) {
                log.error("Error occurred:: ", e);
            }
        }
        return new RedirectView("/payment/error");
    }

    private Order getPendingOrderByCustomerId() {
        return orderService.findPendingOrderByCustomerId(
                customerService.getLoggedInCustomer().getId());
    }

    @GetMapping("/payment/success")
    public String paymentSuccess(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId) {

        Order order = getPendingOrderByCustomerId();
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            if (payment.getState().equals("approved")) {
                orderService.updatePaymentStatus(order, COMPLETED);
                return "paymentSuccess";
            }
        } catch (PayPalRESTException e) {
            log.error("Error occurred:: ", e);
        }
        orderService.updatePaymentStatus(order, COMPLETED);
        return "paymentSuccess";
    }

    @GetMapping("/payment/cancel")
    public String paymentCancel() {
        Order order = getPendingOrderByCustomerId();
        orderService.updatePaymentStatus(order, CANCELLED);
        return "paymentCancel";
    }

    @GetMapping("/payment/error")
    public String paymentError() {
        return "paymentError";
    }
}