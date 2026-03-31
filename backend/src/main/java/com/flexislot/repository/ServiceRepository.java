package com.flexislot.repository;

import com.flexislot.domain.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Service, String> {

    Page<Service> findAllByBusinessId(String businessId, Pageable pageable);
}
