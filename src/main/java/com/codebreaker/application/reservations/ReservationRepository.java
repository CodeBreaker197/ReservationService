package com.codebreaker.application.reservations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    List<ReservationEntity> findAllByStatusIs(ReservationStatus status);

    @Query("""
           SELECT COUNT(r) > 0 FROM ReservationEntity r
           WHERE r.roomId = :roomId
           AND r.status = :status
           AND (:id IS NULL OR r.id != :id)
           AND r.startDate < :endDate
           AND r.endDate > :startDate
           """)
    boolean hasConflict(
            @Param("roomId") Long roomId,
            @Param("status") ReservationStatus status,
            @Param("id") Long id,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Transactional
    @Modifying
    @Query("""
           UPDATE ReservationEntity r
           SET r.status = :status
           WHERE r.id = :id
           """)
    void setStatus(
            @Param("id") Long id,
            @Param("status") ReservationStatus status
    );


    @Query("SELECT r FROM ReservationEntity r WHERE r.roomId = :roomId")
    List<ReservationEntity> findAllReservationsByRoomId(@Param("roomId") Long roomId);
}
