package linx7a.reservation_system.reservations;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservationService {
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation getReservationById(Long id) {
        ReservationEntity reservationEntity = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Брони с ID: " + id + " не найдено."));

        return toDomainReservation(reservationEntity);
    }

    public List<Reservation> getAllReservations() {
        List<ReservationEntity> allEntities = reservationRepository.findAll();
        return allEntities.stream()
                .map(this::toDomainReservation)
                .toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {
        if (reservationToCreate.status() != null) {
            throw new IllegalArgumentException("Статус должен быть пустым.");
        }
        if (!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())) {
            throw new IllegalArgumentException(
                    "Дата начала бронирования должна быть хотя бы на 1 день раньше, чем дата окончания."
            );
        }
        var entityToSave = new ReservationEntity(
                null,
                reservationToCreate.userId(),
                reservationToCreate.roomId(),
                reservationToCreate.startDate(),
                reservationToCreate.endDate(),
                ReservationStatus.PENDING
        );
        var saved = reservationRepository.save(entityToSave);
        return toDomainReservation(saved);
    }

    @Transactional
    public void cancelReservation(Long id) {
        var reservationEntity = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Брони с id: " + id + " не найдено."));
        if (reservationEntity.getStatus() == ReservationStatus.APPROVED) {
            throw new IllegalStateException(
                    "Бронь уже одобрена и не может быть отменена самостоятельно. " +
                            "Пожалуйста, свяжитесь с менеджером."
            );
        }
        if (reservationEntity.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Бронь уже отменена.");
        }
        reservationRepository.setStatus(id, ReservationStatus.CANCELLED);
        log.info("Запись успешно отменена.");
    }

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {
        var reservationEntity = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Брони с id: " + id + " не найдено."));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Невозможно изменить бронь со статусом=" + reservationEntity.getStatus());
        }
        if (!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())) {
            throw new IllegalArgumentException(
                    "Дата начала бронирования должна быть хотя бы на 1 день раньше, чем дата окончания."
            );
        }
        var reservationToSave = new ReservationEntity(
                reservationEntity.getId(),
                reservationToUpdate.userId(),
                reservationToUpdate.roomId(),
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate(),
                ReservationStatus.PENDING
        );
        var updated = reservationRepository.save(reservationToSave);
        return toDomainReservation(updated);
    }

    public Reservation approveReservation(Long id) {
        var reservationEntity = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Брони с id: " + id + " не найдено."));
        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Невозможно изменить бронь со статусом=" + reservationEntity.getStatus());
        }

        List<Long> conflictingIds = reservationRepository.findConflictReservationIds(
                reservationEntity.getRoomId(),
                reservationEntity.getId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate(),
                ReservationStatus.APPROVED
        );
        if (!conflictingIds.isEmpty()) {
            log.info("Конфликт с бронями: {}", conflictingIds);
            throw new IllegalStateException("Бронь пересекается по датам с уже одобренной бронью на эту комнату.");
        }
        reservationEntity.setStatus(ReservationStatus.APPROVED);
        reservationRepository.save(reservationEntity);
        return toDomainReservation(reservationEntity);
    }

    private Reservation toDomainReservation(
            ReservationEntity reservation
    ) {
        return new Reservation(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getRoomId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getStatus()
        );
    }

}
