package com.example.customerdemowithoutdb.service;

import com.example.customerdemowithoutdb.entity.Customer;
import com.example.customerdemowithoutdb.exception.CustomerNotFoundException;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CustomerService {

    private final List<Customer> customers = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public CustomerService() {
        // Add some static customer with email, city, state, country detial
        customers.add(new Customer(idCounter.getAndIncrement(), 
                "John Doe", "john@example.com", "New York", "NY", "USA"));
        customers.add(new Customer(idCounter.getAndIncrement(), 
                "Jane Smith", "jane@example.com", "Los Angeles", "CA", "USA"));
          customers.add(new Customer(idCounter.getAndIncrement(), 
                "Benny Wagen", "Benne@example.com", "Manila", "PA", "USA"));
    }

    public List<Customer> getAllCustomers() {
        return customers;
    }

    public Customer getCustomerById(Long id) {
        return customers.stream()
                .filter(customer -> customer.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    public Customer createCustomer(Customer newCustomer) {
        newCustomer.setId(idCounter.getAndIncrement());
        customers.add(newCustomer);
        return newCustomer;
    }

    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        Optional<Customer> existingCustomer = customers.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();

        if (existingCustomer.isPresent()) {
            Customer customer = existingCustomer.get();
            customer.setName(updatedCustomer.getName());
            customer.setEmail(updatedCustomer.getEmail());
            customer.setCity(updatedCustomer.getCity());
            customer.setState(updatedCustomer.getState());
            customer.setCountry(updatedCustomer.getCountry()); // ✅ update country too
            return customer;
        } else {
            throw new CustomerNotFoundException(id); 
        }
    }

    public void deleteCustomer(Long id) {
        boolean removed = customers.removeIf(customer -> customer.getId().equals(id));
        if (!removed) {
        	 throw new CustomerNotFoundException(id); 
        }
    }
}

