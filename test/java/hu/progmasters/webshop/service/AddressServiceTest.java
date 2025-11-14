package hu.progmasters.webshop.service;

import hu.progmasters.webshop.domain.Address;
import hu.progmasters.webshop.domain.Customer;
import hu.progmasters.webshop.domain.CustomerAddress;
import hu.progmasters.webshop.dto.incoming.AddressCreateUpdateCommand;
import hu.progmasters.webshop.dto.outgoing.AddressInfo;
import hu.progmasters.webshop.exception.AddressDoesNotBelongToCustomerException;
import hu.progmasters.webshop.exception.AddressNotExistsException;
import hu.progmasters.webshop.repository.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static hu.progmasters.webshop.domain.enumeration.LotrCity.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AddressService addressService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAddress_whenAddressDoesNotExist_createsNewAddress() {
        AddressCreateUpdateCommand command = new AddressCreateUpdateCommand();
        Long customerId = 1L;
        Customer customer = new Customer();
        Address address = new Address();

        when(customerService.findCustomerById(customerId)).thenReturn(customer);
        doNothing().when(customerService).compareLoggedInCustomerToCustomerToMakeOperationOn(customer);
        when(modelMapper.map(command, Address.class)).thenReturn(address);
        when(addressRepository.save(address)).thenReturn(address);

        addressService.createAddress(command, customerId);

        verify(addressRepository, times(1)).save(address);
    }

    @Test
    void createAddress_whenAddressExistsAndNotLinkedToCustomer_linksAddressToCustomer() {
        AddressCreateUpdateCommand command = new AddressCreateUpdateCommand();
        Long customerId = 1L;
        Customer customer = new Customer();
        Address address = new Address();

        when(customerService.findCustomerById(customerId)).thenReturn(customer);
        doNothing().when(customerService).compareLoggedInCustomerToCustomerToMakeOperationOn(customer);
        when(addressRepository.getAddressByZipAndCityAndStreetAndHouseNumber(
                command.getZip(),
                command.getCity(),
                command.getStreet(),
                command.getHouseNumber())).thenReturn(address);

        addressService.createAddress(command, customerId);

        verify(addressRepository, times(0)).save(address);
    }

    @Test
    void updateAddress_whenAddressExistsAndLinkedToCustomer_updatesAddress() {
        AddressCreateUpdateCommand command = new AddressCreateUpdateCommand();
        command.setZip("12345");
        command.setCity(BRIE);
        command.setStreet("Test Street");
        command.setHouseNumber("123");
        Long customerId = 1L;
        Long addressId = 1L;
        Customer customer = new Customer();
        customer.setId(customerId);
        Address address = new Address();
        address.setZip(command.getZip());
        address.setCity(command.getCity());
        address.setStreet(command.getStreet());
        address.setHouseNumber(command.getHouseNumber());
        CustomerAddress customerAddress = new CustomerAddress();
        customerAddress.setCustomer(customer);
        customerAddress.setAddress(address);
        address.getCustomerAddressList().add(customerAddress);

        when(customerService.findCustomerById(customerId)).thenReturn(customer);
        doNothing().when(customerService).compareLoggedInCustomerToCustomerToMakeOperationOn(customer);
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(modelMapper.map(command, Address.class)).thenReturn(address);

        AddressInfo result = addressService.updateAddress(command, addressId, customerId);

        assertNull(result);
    }

    @Test
    void updateAddress_whenAddressNotLinkedToCustomer_throwsException() {
        AddressCreateUpdateCommand command = new AddressCreateUpdateCommand();
        Long customerId = 1L;
        Long addressId = 1L;
        Customer customer = new Customer();
        Address address = new Address();

        when(customerService.findCustomerById(customerId)).thenReturn(customer);
        doNothing().when(customerService).compareLoggedInCustomerToCustomerToMakeOperationOn(customer);
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));

        assertThrows(AddressDoesNotBelongToCustomerException.class, () -> addressService.updateAddress(command,
                                                                                                       addressId,
                                                                                                       customerId));
    }

    @Test
    void deleteAddress_whenAddressExistsAndLinkedToCustomer_removesAddress() {
        Long customerId = 1L;
        Long addressId = 1L;
        Customer customer = new Customer();
        customer.setId(customerId); // Set id for the customer
        Address address = new Address();
        CustomerAddress customerAddress = new CustomerAddress();
        customerAddress.setCustomer(customer);
        customerAddress.setAddress(address);
        address.getCustomerAddressList().add(customerAddress);

        when(customerService.findCustomerById(customerId)).thenReturn(customer);
        doNothing().when(customerService).compareLoggedInCustomerToCustomerToMakeOperationOn(customer);
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));

        addressService.deleteAddress(addressId, customerId);

        verify(addressRepository, times(1)).removeCustomerAddress(addressId, customerId);
    }

    @Test
    void deleteAddress_whenAddressDoesNotExist_throwsException() {
        Long customerId = 1L;
        Long addressId = 1L;
        Customer customer = new Customer();

        when(customerService.findCustomerById(customerId)).thenReturn(customer);
        doNothing().when(customerService).compareLoggedInCustomerToCustomerToMakeOperationOn(customer);
        when(addressRepository.findById(addressId)).thenReturn(Optional.empty());

        assertThrows(AddressNotExistsException.class, () -> addressService.deleteAddress(addressId, customerId));
    }

    @Test
    void deleteAddress_whenAddressNotLinkedToCustomer_throwsException() {
        Long customerId = 1L;
        Long addressId = 1L;
        Customer customer = new Customer();
        Address address = new Address();

        when(customerService.findCustomerById(customerId)).thenReturn(customer);
        doNothing().when(customerService).compareLoggedInCustomerToCustomerToMakeOperationOn(customer);
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));

        assertThrows(AddressDoesNotBelongToCustomerException.class, () -> addressService.deleteAddress(addressId,
                                                                                                       customerId));
    }

    @Test
    void getAddressByCustomerId_whenCustomerExistsAndHasAddresses_returnsAddressInfoList() {
        Long customerId = 1L;
        Customer customer = new Customer();
        customer.setId(customerId);
        Address address1 = new Address();
        Address address2 = new Address();
        List<Address> addresses = Arrays.asList(address1, address2);

        when(customerService.findCustomerById(customerId)).thenReturn(customer);
        when(addressRepository.findAllByCustomerId(customerId)).thenReturn(addresses);

        List<AddressInfo> result = addressService.getAddressByCustomerId(customerId);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getAddressByCustomerId_whenCustomerExistsAndHasNoAddresses_returnsEmptyList() {
        Long customerId = 1L;
        Customer customer = new Customer();
        customer.setId(customerId);
        List<Address> addresses = new ArrayList<>();

        when(customerService.findCustomerById(customerId)).thenReturn(customer);
        when(addressRepository.findAllByCustomerId(customerId)).thenReturn(addresses);

        List<AddressInfo> result = addressService.getAddressByCustomerId(customerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAddressByCustomerId_whenCustomerDoesNotExist_returnsEmptyList() {
        Long customerId = 1L;

        when(customerService.findCustomerById(customerId)).thenReturn(null);

        List<AddressInfo> result = addressService.getAddressByCustomerId(customerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}