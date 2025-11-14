package hu.progmasters.webshop.dto.outgoing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemInfo2 {

    private Long productId;

    private String productName;

    private Integer piecesOrdered;

    private Double totalPrice;
}
