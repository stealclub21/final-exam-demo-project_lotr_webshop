package hu.progmasters.webshop.controller;

import hu.progmasters.webshop.dto.incoming.RatingCreateCommand;
import hu.progmasters.webshop.dto.outgoing.RatingInfo;
import hu.progmasters.webshop.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("lotr-webshop/ratings")
@Slf4j
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/{customerId}")
    @ResponseStatus(CREATED)
    public RatingInfo createRating(@Valid @RequestBody RatingCreateCommand command, @PathVariable Long customerId) {
        log.info("Http request, POST / lotr-webshop / ratings / customerId, body: {}", command.toString());
        return ratingService.createRating(command, customerId);
    }
}
