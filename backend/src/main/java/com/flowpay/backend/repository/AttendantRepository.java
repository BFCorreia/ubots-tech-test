package com.flowpay.backend.repository;

import com.flowpay.backend.domain.Attendant;
import com.flowpay.backend.domain.Team;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AttendantRepository extends JpaRepository<Attendant, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Attendant a WHERE a.team = :team AND " +
           "(SELECT COUNT(t) FROM Ticket t WHERE t.attendant.id = a.id AND t.status = 'IN_PROGRESS') < 3 " +
           "ORDER BY (SELECT COUNT(t) FROM Ticket t WHERE t.attendant.id = a.id AND t.status = 'IN_PROGRESS') ASC")
    List<Attendant> findAvailableAttendantsWithLock(@Param("team") Team team, Pageable pageable);
}
