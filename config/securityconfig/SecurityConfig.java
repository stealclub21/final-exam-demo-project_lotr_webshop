package hu.progmasters.webshop.config.securityconfig;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter.Directive.COOKIES;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
@AllArgsConstructor
public class SecurityConfig {

    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthUserService authUserService;

    private static final String ROLE_ADMIN= "ROLE_ADMIN";
    private static final String ROLE_PREMIUMUSER= "ROLE_PREMIUM";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(form -> form
                               .loginPage("/lotr-webshop/registration/custom-login")
                               .defaultSuccessUrl("/lotr-webshop/customers/me", true)
                               .failureUrl("/error-general?message=Login failed! Wrong username or password or account has not yet been activated!")
                               .permitAll()
                      )

            .logout(logout -> {
                logout.addLogoutHandler(new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(COOKIES)));
                logout.logoutUrl("/lotr-webshop/customers/logout");
                logout.deleteCookies("JSESSIONID");
                logout.logoutSuccessUrl("/login");
            })
            .authorizeHttpRequests(requests -> requests
                                           .requestMatchers("/error-general").permitAll()
                                           .requestMatchers("/go-to-email").permitAll()
                                           .requestMatchers("/error-really").permitAll()
                                           .requestMatchers("/lotr-webshop/registration/**").permitAll()
                                           .requestMatchers(HttpMethod.GET, "/lotr-webshop/registration/confirm/**").permitAll()
                                           .requestMatchers(HttpMethod.PUT, "/lotr-webshop/customers/**").permitAll()
                                           .requestMatchers("/lotr-webshop/bombadils-emporium/**").hasAnyAuthority(ROLE_ADMIN, ROLE_PREMIUMUSER)
                                           .requestMatchers("/lotr-webshop/productcategories/**").hasAuthority(ROLE_ADMIN)
                                           .requestMatchers("/lotr-webshop/products/admin/**").hasAnyAuthority(ROLE_ADMIN, ROLE_PREMIUMUSER)
                                           .requestMatchers("/lotr-webshop/products/add-to-stock/**").hasAnyAuthority(ROLE_ADMIN, ROLE_PREMIUMUSER)
                                           .requestMatchers("/lotr-webshop/products/remove-from-stock/**").hasAnyAuthority(ROLE_ADMIN, ROLE_PREMIUMUSER)
                                           .requestMatchers(HttpMethod.GET, "/lotr-webshop/orders").hasAuthority(ROLE_ADMIN)
                                           .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                                           .anyRequest().authenticated()
                                  )
            .httpBasic(withDefaults())
            .authenticationProvider(daoAuthenticationProvider())
            .sessionManagement(sessionManagement -> {
                                   sessionManagement.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
                                   sessionManagement
                                           .maximumSessions(1)
                                           .maxSessionsPreventsLogin(false);
                               }
                              );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(authUserService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
