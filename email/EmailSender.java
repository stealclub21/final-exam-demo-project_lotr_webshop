package hu.progmasters.webshop.email;


public interface EmailSender {

    void send(String to, String email, String subject);
}
