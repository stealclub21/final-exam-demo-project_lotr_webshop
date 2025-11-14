package hu.progmasters.webshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SuccessChangePasswordController {

    @GetMapping("/lotr-webshop/registration/success-changing-password")
    public String errorGeneral() {
        return "successPasswordChange";
    }
}
