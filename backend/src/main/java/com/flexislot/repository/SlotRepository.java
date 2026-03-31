package com.flexislot.repository;

import com.flexislot.domain.Slot;
import com.flexislot.domain.enums.SlotStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SlotRepository extends JpaRepository<Slot, String> {

    Page<Slot> findByServiceIdAndStatus(String serviceId, SlotStatus status, Pageable pageable);

    @Query("SELECT s FROM Slot s WHERE s.serviceId = :serviceId AND s.status = :status " +
            "AND (:fromDate IS NULL OR s.slotDate >= :fromDate) AND (:toDate IS NULL OR s.slotDate <= :toDate)")
    Page<Slot> findByServiceIdAndStatusAndDateRange(
            @Param("serviceId") String serviceId,
            @Param("status") SlotStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

    long countByServiceId(String serviceId);

    long countByServiceIdAndStatus(String serviceId, SlotStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Slot s WHERE s.id = :slotId")
    Optional<Slot> findByIdWithPessimisticLock(@Param("slotId") String slotId);

    boolean existsByServiceIdAndSlotDateAndStartTimeAndEndTime(String serviceId, LocalDate slotDate,
            java.time.LocalTime startTime, java.time.LocalTime endTime);
}
