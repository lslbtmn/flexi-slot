package com.flexislot.config;

import com.flexislot.domain.Business;
import com.flexislot.domain.Customer;
import com.flexislot.domain.User;
import com.flexislot.domain.enums.UserRole;
import com.flexislot.repository.BusinessRepository;
import com.flexislot.repository.CustomerRepository;
import com.flexislot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class SeedDataRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Seed data already present, skipping.");
            return;
        }
        log.info("Seeding demo data...");
        User admin = User.builder()
                .email("admin@flexislot.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role(UserRole.ADMIN)
                .isActive(true)
                .build();
        admin = userRepository.save(admin);

        User owner = User.builder()
                .email("owner@flexislot.com")
                .passwordHash(passwordEncoder.encode("owner123"))
                .role(UserRole.BUSINESS_OWNER)
                .isActive(true)
                .build();
        owner = userRepository.save(owner);

        Business business = Business.builder()
                .ownerUserId(owner.getId())
                .name("Demo Salon")
                .email("salon@demo.com")
                .phone("+1234567890")
                .location("123 Main St")
                .serviceType("Beauty")
                .operatingHours("{\"mon-fri\":\"9:00-18:00\",\"sat\":\"10:00-16:00\"}")
                .build();
        business = businessRepository.save(business);

        User customerUser = User.builder()
                .email("customer@flexislot.com")
                .passwordHash(passwordEncoder.encode("customer123"))
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .build();
        customerUser = userRepository.save(customerUser);

        Customer customer = Customer.builder()
                .userId(customerUser.getId())
                .name("Jane Doe")
                .email("customer@flexislot.com")
                .phone("+0987654321")
                .build();
        customerRepository.save(customer);

        log.info("Seed data created: admin@flexislot.com, owner@flexislot.com, customer@flexislot.com (passwords: admin123, owner123, customer123)");
    }
}
