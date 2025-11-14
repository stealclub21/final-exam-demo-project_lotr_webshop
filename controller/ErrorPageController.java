package hu.progmasters.webshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorPageController {

    @GetMapping("/error-general")
    public String errorGeneral() {
        return "errorGeneral";
    }

    @GetMapping("/error-really")
    public String errorReally() {
        return "really";
    }
}
