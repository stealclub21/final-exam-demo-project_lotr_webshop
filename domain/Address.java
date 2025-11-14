package hu.progmasters.webshop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import hu.progmasters.webshop.domain.enumeration.AddressType;
import hu.progmasters.webshop.domain.enumeration.LotrCity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Enumerated(EnumType.STRING)
    private AddressType addressType;

    @NonNull
    private String zip;

    @NonNull
    @Enumerated(EnumType.STRING)
    private LotrCity city;

    @NonNull
    private String street;

    @NonNull
    private String houseNumber;

    @JsonIgnore
    @OneToMany(mappedBy = "address", cascade = CascadeType.PERSIST)
    private List<CustomerAddress> customerAddressList = new ArrayList<>();
}
