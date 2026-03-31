package com.flexislot.repository;

import com.flexislot.domain.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    Page<Booking> findByCustomerIdOrderByBookingTimestampDesc(String customerId, Pageable pageable);

    boolean existsBySlotId(String slotId);
}
