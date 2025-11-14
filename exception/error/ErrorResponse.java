package hu.progmasters.webshop.exception.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ErrorResponse {

    private String field;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime timestamp;

    private String message;

    private List<String> errors;


    public ErrorResponse(String field, String message) {
        this.field = field;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(List<String> errors) {
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
    }
}
