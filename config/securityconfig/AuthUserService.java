package hu.progmasters.webshop.config.securityconfig;

import hu.progmasters.webshop.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class AuthUserService implements UserDetailsService {

    private static final String USER_NOT_FOUND_MSG = "User with email %s not found";
    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return customerRepository
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, email)));
    }


    public UserDetails getUserFromSession() {
        return (UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
