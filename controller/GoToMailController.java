package hu.progmasters.webshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GoToMailController {

    @GetMapping("/go-to-email")
    public String errorGeneral() {
        return "goToYourEmail";
    }
}
