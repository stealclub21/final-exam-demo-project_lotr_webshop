package hu.progmasters.webshop.dto.outgoing;

import com.fasterxml.jackson.annotation.JsonFormat;
import hu.progmasters.webshop.domain.Address;
import hu.progmasters.webshop.domain.enumeration.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderInfo2 {

    @JsonFormat(pattern = "yyyy-MM-dd - HH:mm:ss")
    private LocalDateTime orderDate = LocalDateTime.now();

    private String customerName;

    @NotNull
    private Address address;

    private Double totalPriceOfOrder;

    private OrderStatus orderStatus;

    private List<OrderItemInfo2> orderItemInfoList;
}
