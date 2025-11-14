package hu.progmasters.webshop.service;

import hu.progmasters.webshop.config.ObjectMapperUtil;
import hu.progmasters.webshop.domain.Address;
import hu.progmasters.webshop.domain.Customer;
import hu.progmasters.webshop.domain.CustomerAddress;
import hu.progmasters.webshop.dto.incoming.AddressCreateUpdateCommand;
import hu.progmasters.webshop.dto.outgoing.AddressInfo;
import hu.progmasters.webshop.exception.AddressDoesNotBelongToCustomerException;
import hu.progmasters.webshop.exception.AddressNotExistsException;
import hu.progmasters.webshop.exception.CustomerAlreadyLinkedToAddressException;
import hu.progmasters.webshop.repository.AddressRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final CustomerService customerService;
    private final ModelMapper modelMapper;

    public AddressInfo createAddress(AddressCreateUpdateCommand command, Long customerId) {

        Customer customer = returnCustomerIfLoggedInCustomerEqualsActionRequestCustomer(customerId);

        Address address = getAddressIfAlreadyExists(command);

        if (address == null) {
            address = modelMapper.map(command, Address.class);
            address = addressRepository.save(address);
        }

        boolean isAddressAlreadyLinkedToCustomer = isAddressAlreadyLinkedToCustomer(customerId, address);

        if (isAddressAlreadyLinkedToCustomer) {
            throw new CustomerAlreadyLinkedToAddressException(customerId);
        }

        setCustomerAndAddressToCustomerAddress(customer, address);
        return modelMapper.map(address, AddressInfo.class);
    }

    public AddressInfo updateAddress(AddressCreateUpdateCommand command, Long addressId, Long customerId) {

        returnCustomerIfLoggedInCustomerEqualsActionRequestCustomer(customerId);

        Address address = findAddressById(addressId);

        if (!isAddressAlreadyLinkedToCustomer(customerId, address)) {
            throw new AddressDoesNotBelongToCustomerException(addressId, customerId);
        }
        modelMapper.map(command, address);
        return modelMapper.map(address, AddressInfo.class);
    }


    public void deleteAddress(Long addressId, Long customerId) {

        returnCustomerIfLoggedInCustomerEqualsActionRequestCustomer(customerId);

        Address address = findAddressById(addressId);

        if (!isAddressAlreadyLinkedToCustomer(customerId, address)) {
            throw new AddressDoesNotBelongToCustomerException(addressId, customerId);
        }
        addressRepository.removeCustomerAddress(addressId, customerId);
    }

    public List<AddressInfo> getAddressByCustomerId(Long customerId) {

        returnCustomerIfLoggedInCustomerEqualsActionRequestCustomer(customerId);

        List<Address> addresses = addressRepository.findAllByCustomerId(customerId);
        return ObjectMapperUtil.mapAll(addresses, AddressInfo.class);
    }

    private Customer returnCustomerIfLoggedInCustomerEqualsActionRequestCustomer(Long customerId) {
        Customer customer = customerService.findCustomerById(customerId);
        customerService.compareLoggedInCustomerToCustomerToMakeOperationOn(customer);
        return customer;
    }

    private Address findAddressById(Long addressId) {
        return addressRepository.findById(addressId).orElseThrow(() -> new AddressNotExistsException(addressId));
    }

    private void setCustomerAndAddressToCustomerAddress(Customer customer, Address address) {
        CustomerAddress customerAddress = new CustomerAddress();
        customerAddress.setCustomer(customer);
        customerAddress.setAddress(address);
        customer.getCustomerAddressList().add(customerAddress);
    }

    public boolean isAddressAlreadyLinkedToCustomer(Long customerId, Address address) {
        return address.getCustomerAddressList().stream()
                      .anyMatch(customerAddress -> customerAddress
                              .getCustomer()
                              .getId()
                              .equals(customerId));
    }

    private Address getAddressIfAlreadyExists(AddressCreateUpdateCommand command) {
        return addressRepository
                .getAddressByZipAndCityAndStreetAndHouseNumber(
                        command.getZip(),
                        command.getCity(),
                        command.getStreet(),
                        command.getHouseNumber());
    }
}
