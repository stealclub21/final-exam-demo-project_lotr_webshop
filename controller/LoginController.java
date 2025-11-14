package hu.progmasters.webshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("lotr-webshop/registration")
public class LoginController {

    @GetMapping("/custom-login")
    public String customLogin() {
        return "custom_login";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "reset_password";
    }

    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);

        return "register_new_password";
    }
}
