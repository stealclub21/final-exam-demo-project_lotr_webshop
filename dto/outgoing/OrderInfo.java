package hu.progmasters.webshop.dto.outgoing;

import com.fasterxml.jackson.annotation.JsonFormat;
import hu.progmasters.webshop.domain.OrderItem;
import hu.progmasters.webshop.domain.enumeration.OrderStatus;
import hu.progmasters.webshop.domain.enumeration.ShippingMethod;
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
public class OrderInfo {

    private Long orderId;

    @JsonFormat(pattern = "yyyy-MM-dd - HH:mm:ss")
    private LocalDateTime orderDate;

    private Double totalPriceOfOrder;

    private String comments;

    private OrderStatus orderStatus = OrderStatus.NEW;

    private Long customerId;

    private ShippingMethod shippingMethod;

    private List<OrderItemInfo> orderItemInfoList;

}
