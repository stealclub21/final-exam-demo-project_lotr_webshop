package hu.progmasters.webshop.dto.outgoing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemInfo {

    private Long orderItemId;

    private Integer piecesOrdered;

    private Double totalPrice;

    private Long productId;

    private Long orderId;

}
