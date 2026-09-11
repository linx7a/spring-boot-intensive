package linx7a.reservation_system.service;

import linx7a.reservation_system.model.Reservation;
import linx7a.reservation_system.model.ReservationStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReservationService {
    private final Map<Long, Reservation> reservationMap;

    private final AtomicLong idCounter;

    public ReservationService() {
        reservationMap = new HashMap<>();
        idCounter = new AtomicLong();
    }

    public Reservation getReservationById(Long id) {
        if (!reservationMap.containsKey(id)) {
            throw new NoSuchElementException("Брони с ID: " + id + " не найдено.");
        }
        return reservationMap.get(id);
    }

    public List<Reservation> getAllReservations() {
        if (reservationMap.isEmpty()) {
            throw new RuntimeException("Ничего не найдено.");
        }
        return reservationMap.values().stream().toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {
        if (reservationToCreate.id() != null) {
            throw new IllegalArgumentException("ID должен быть пустым.");
        }
        if (reservationToCreate.status() != null) {
            throw new IllegalArgumentException("Статус должен быть пустым.");
        }
        var newReservation = new Reservation(
                idCounter.incrementAndGet(),
                reservationToCreate.userId(),
                reservationToCreate.roomId(),
                reservationToCreate.startDate(),
                reservationToCreate.endDate(),
                ReservationStatus.PENDING
        );

        reservationMap.put(newReservation.id(), newReservation);
        return newReservation;
    }

    public void deleteReservation(Long id) {
        if (!reservationMap.containsKey(id)) {
            throw new NoSuchElementException("Брони с id: " + id + " не найдено.");
        }
        reservationMap.remove(id);
    }

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {
        if (!reservationMap.containsKey(id)) {
            throw new NoSuchElementException("Брони с id: " + id + " не найдено.");
        }
        var reservation = reservationMap.get(id);
        if (reservation.status() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Невозможно изменить бронь со статусом=" + reservation.status());
        }
        var updatedReservation = new Reservation(
                reservation.id(),
                reservationToUpdate.userId(),
                reservationToUpdate.roomId(),
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate(),
                ReservationStatus.PENDING
        );
        reservationMap.put(reservation.id(), updatedReservation);
        return updatedReservation;
    }

    public Reservation approveReservation(Long id) {
        if (!reservationMap.containsKey(id)) {
            throw new NoSuchElementException("Брони с id: " + id + " не найдено.");
        }
        var reservation = reservationMap.get(id);
        if (reservation.status() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Невозможно изменить бронь со статусом=" + reservation.status());
        }
        boolean hasOverlap = reservationMap.values().stream()
                .filter(other -> !other.id().equals(id))
                .filter(other -> other.roomId().equals(reservation.roomId()))
                .filter(other -> other.status() == ReservationStatus.APPROVED)
                .anyMatch(other -> reservation.startDate().isBefore(other.endDate())
                        && other.startDate().isBefore(reservation.endDate())
                );
        if (hasOverlap) {
            throw new IllegalStateException("Бронь пересекается по датам с уже одобренной бронью на эту комнату.");
        }
        var approvedReservation = new Reservation(
                reservation.id(),
                reservation.userId(),
                reservation.roomId(),
                reservation.startDate(),
                reservation.endDate(),
                ReservationStatus.APPROVED
        );
        reservationMap.put(reservation.id(), approvedReservation);
        return approvedReservation;
    }

}
