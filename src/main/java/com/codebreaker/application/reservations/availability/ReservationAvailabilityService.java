package com.codebreaker.application.reservations.availability;

import com.codebreaker.application.reservations.ReservationRepository;
import com.codebreaker.application.reservations.ReservationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationAvailabilityService {

    Logger log = LoggerFactory.getLogger(ReservationAvailabilityService.class);

    private final ReservationRepository repository;

    public ReservationAvailabilityService(ReservationRepository repository) {
        this.repository = repository;
    }

    public boolean  isReservationAvailable(
            Long roomId,
            LocalDate startDate,
            LocalDate endDate
    ) {

        if(startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        List<Long> conflictingIds = repository.hasConflict(
                roomId,
                startDate,
                endDate,
                ReservationStatus.APPROVED
        );

        if(conflictingIds.isEmpty()) {
            return true;
        }
        log.info("Conflicting with ids: ids={}", conflictingIds);
        return false;
    }

}
