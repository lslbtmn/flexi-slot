package com.flexislot.repository;

import com.flexislot.domain.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessRepository extends JpaRepository<Business, String> {

    Optional<Business> findByOwnerUserId(String ownerUserId);

    boolean existsByOwnerUserId(String ownerUserId);
}
