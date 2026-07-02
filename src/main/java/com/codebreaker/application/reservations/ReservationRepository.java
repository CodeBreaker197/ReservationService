package com.codebreaker.application.reservations;

import org.springframework.data.domain.Pageable;
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
           SELECT r.id FROM ReservationEntity r
           WHERE r.roomId = :roomId
           AND :startDate < r.endDate
           AND r.startDate < :endDate
           AND r.status = :status
           """)
    List<Long> hasConflict(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") ReservationStatus status
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

    @Query("""
           SELECT r FROM ReservationEntity r
               WHERE (:roomId IS NULL OR r.roomId = :roomId)
               AND (:userId IS NULL OR r.userId = :userId)
           """)
    List<ReservationEntity> searchAllByFilter(
            @Param("userId") Long userId,
            @Param("roomId") Long roomId,
            Pageable pageable
    );
}
