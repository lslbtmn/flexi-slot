package com.flexislot.repository;

import com.flexislot.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {

    Optional<Customer> findByUserId(String userId);

    boolean existsByUserId(String userId);
}
