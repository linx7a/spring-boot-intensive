package linx7a.reservation_system.service;

import jakarta.persistence.EntityNotFoundException;
import linx7a.reservation_system.entity.ReservationEntity;
import linx7a.reservation_system.model.Reservation;
import linx7a.reservation_system.model.ReservationStatus;
import linx7a.reservation_system.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationService {
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
        if (reservationToCreate.id() != null) {
            throw new IllegalArgumentException("ID должен быть пустым.");
        }
        if (reservationToCreate.status() != null) {
            throw new IllegalArgumentException("Статус должен быть пустым.");
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

    public void deleteReservation(Long id) {
        var reservationEntity = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Брони с id: " + id + " не найдено."));
        reservationRepository.deleteById(id);
    }

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {
        var reservationEntity = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Брони с id: " + id + " не найдено."));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Невозможно изменить бронь со статусом=" + reservationEntity.getStatus());
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
        boolean hasOverlap = reservationRepository.findAll().stream()
                .filter(other -> !other.getId().equals(id))
                .filter(other -> other.getRoomId().equals(reservationEntity.getRoomId()))
                .filter(other -> other.getStatus() == ReservationStatus.APPROVED)
                .anyMatch(other -> reservationEntity.getStartDate().isBefore(other.getEndDate())
                        && other.getStartDate().isBefore(reservationEntity.getEndDate())
                );
        if (hasOverlap) {
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
