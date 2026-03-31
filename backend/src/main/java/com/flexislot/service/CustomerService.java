package com.flexislot.service;

import com.flexislot.domain.Customer;
import com.flexislot.dto.customer.CustomerRequest;
import com.flexislot.dto.customer.CustomerResponse;
import com.flexislot.exception.AppException;
import com.flexislot.exception.ForbiddenException;
import com.flexislot.exception.ResourceNotFoundException;
import com.flexislot.repository.CustomerRepository;
import com.flexislot.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    @PreAuthorize("hasRole('CUSTOMER')")
    public CustomerResponse create(CustomerRequest request, UserPrincipal principal) {
        if (customerRepository.existsByUserId(principal.getId())) {
            throw new AppException("Customer profile already exists", HttpStatus.BAD_REQUEST);
        }
        Customer customer = Customer.builder()
                .userId(principal.getId())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
        customer = customerRepository.save(customer);
        log.info("Created customer {} for user {}", customer.getId(), principal.getId());
        return toResponse(customer);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public CustomerResponse getById(String id, UserPrincipal principal) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        if (principal.getRole() != com.flexislot.domain.enums.UserRole.ADMIN && !customer.getUserId().equals(principal.getId())) {
            throw new ForbiddenException("Not allowed to view this customer");
        }
        return toResponse(customer);
    }

    @Transactional
    @PreAuthorize("hasRole('CUSTOMER')")
    public CustomerResponse update(String id, CustomerRequest request, UserPrincipal principal) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        if (!customer.getUserId().equals(principal.getId())) {
            throw new ForbiddenException("Not allowed to update this customer");
        }
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer = customerRepository.save(customer);
        return toResponse(customer);
    }

    @Transactional
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public void delete(String id, UserPrincipal principal) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        if (principal.getRole() != com.flexislot.domain.enums.UserRole.ADMIN && !customer.getUserId().equals(principal.getId())) {
            throw new ForbiddenException("Not allowed to delete this customer");
        }
        customerRepository.delete(customer);
    }

    @Transactional(readOnly = true)
    public String getCustomerIdForUser(UserPrincipal principal) {
        return customerRepository.findByUserId(principal.getId())
                .map(Customer::getId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('CUSTOMER')")
    public CustomerResponse getCurrentCustomer(UserPrincipal principal) {
        Customer customer = customerRepository.findByUserId(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found. Create one first."));
        return toResponse(customer);
    }

    private CustomerResponse toResponse(Customer c) {
        return CustomerResponse.builder()
                .id(c.getId())
                .userId(c.getUserId())
                .name(c.getName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
