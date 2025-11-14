package hu.progmasters.webshop.dto.incoming;


import hu.progmasters.webshop.domain.enumeration.MessageType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {

    private String content;

    private String sender;

    private MessageType type;

    private String recipient;
}
