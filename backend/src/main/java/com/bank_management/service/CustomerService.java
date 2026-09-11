package com.bank_management.service;

import com.bank_management.dto.CustomerRequest;
import com.bank_management.dto.CustomerResponse;
import com.bank_management.entity.Customer;
import com.bank_management.exception.CustomerAlreadyExistsException;
import com.bank_management.repository.CustomerRepository;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse createCustomer(CustomerRequest request) {
        if(customerRepository.existsByEmail(request.getEmail())){
            throw new CustomerAlreadyExistsException("Customer with email " + request.getEmail() + " already exists.");
        }
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());
        customer.setPhone(request.getPhone());
        Customer savedCustomer = customerRepository.save(customer);
        return new CustomerResponse(
                savedCustomer.getId(),
                savedCustomer.getName(),
                savedCustomer.getEmail(),
                savedCustomer.getPhone(),
                savedCustomer.getAddress()
        );
    }

}
