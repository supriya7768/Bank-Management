package com.bank_management.service;

import com.bank_management.entity.Customer;
import com.bank_management.repository.CustomerRepository;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(Customer customer) {
        if(customerRepository.existsByEmail(customer.getEmail())){
            throw new RuntimeException("Customer with email " + customer.getEmail() + " already exists.");
        }
        return customerRepository.save(customer);
    }

}
