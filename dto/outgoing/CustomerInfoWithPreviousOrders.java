package hu.progmasters.webshop.dto.outgoing;

import hu.progmasters.webshop.domain.CustomerAddress;
import hu.progmasters.webshop.domain.enumeration.CustomerType;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerInfoWithPreviousOrders {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private CustomerType customerType;

    private List<CustomerAddress> customerAddressList = new ArrayList<>();

    private List<OrderInfo> orderList = new ArrayList<>();
}
