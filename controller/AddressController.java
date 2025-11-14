package hu.progmasters.webshop.controller;

import hu.progmasters.webshop.dto.incoming.AddressCreateUpdateCommand;
import hu.progmasters.webshop.dto.outgoing.AddressInfo;
import hu.progmasters.webshop.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("lotr-webshop/addresses")
@Slf4j
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping("/{customerId}")
    @ResponseStatus(CREATED)
    public AddressInfo createAddress(@Valid @RequestBody AddressCreateUpdateCommand command,
                                     @PathVariable Long customerId) {
        log.info("Http request, POST / lotr-webshop / addresses / customerId, body: {}", command.toString());
        return addressService.createAddress(command, customerId);
    }

    @PutMapping("/update/{addressId}/{customerId}")
    @ResponseStatus(OK)
    public AddressInfo updateAddress(@Valid @RequestBody AddressCreateUpdateCommand command,
                                     @PathVariable Long addressId,
                                     @PathVariable Long customerId) {
        log.info("Http request, PUT / lotr-webshop / addresses / update / addressId / customerId, body: {}", command.toString());
        return addressService.updateAddress(command, addressId, customerId);
    }

    @DeleteMapping("/delete/{addressId}/{customerId}")
    @ResponseStatus(NO_CONTENT)
    public void deleteAddress(@PathVariable Long addressId, @PathVariable Long customerId) {
        log.info("Http request, DELETE / lotr-webshop / addresses / delete / addressId / customerId");
        addressService.deleteAddress(addressId, customerId);
    }

    @GetMapping("/get/{customerId}")
    @ResponseStatus(OK)
    public List<AddressInfo> getAddressesByCustomerId(@PathVariable Long customerId) {
        log.info("Http request, GET / lotr-webshop / addresses / get / customerId");
        return addressService.getAddressByCustomerId(customerId);
    }
}
