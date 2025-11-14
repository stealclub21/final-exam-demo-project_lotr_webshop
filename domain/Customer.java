package hu.progmasters.webshop.domain;

import hu.progmasters.webshop.domain.enumeration.CustomerType;
import hu.progmasters.webshop.domain.enumeration.Role;
import hu.progmasters.webshop.domain.enumeration.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Slf4j
@Table(name = "customer", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")})
public class Customer implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private String firstName;

    @NonNull
    private String lastName;

    @NonNull
    private String email;

    @NonNull
    private String password;

    private boolean isActive = false;

    private boolean enabled = false;

    private boolean accountNonExpired = true;

    private boolean accountNonLocked = false;

    private boolean credentialsNonExpired = true;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "role")
    private List<Role> roles = new ArrayList<>(List.of(Role.ROLE_BASICUSER));

    @JoinTable(name = "subscription_status")
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.UNSUBSCRIBED;

    @NonNull
    @Enumerated(EnumType.STRING)
    private CustomerType customerType;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.PERSIST)
    private List<CustomerAddress> customerAddressList = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.PERSIST)
    private List<Order> orderList = new ArrayList<>();

    @OneToOne(mappedBy = "customer", cascade = CascadeType.PERSIST)
    private TotalSpending totalSpending;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays
                .stream(this
                                .getRoles()
                                .stream()
                                .map(Enum::name)
                                .toArray(String[]::new))
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(firstName, customer.firstName)
               && Objects.equals(lastName, customer.lastName)
               && Objects.equals(email, customer.email)
               && Objects.equals(password, customer.password)
               && customerType.equals(customer.customerType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, email, password, customerType);
    }
}
