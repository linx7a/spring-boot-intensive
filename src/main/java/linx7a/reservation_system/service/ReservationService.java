package linx7a.reservation_system.service;

import linx7a.reservation_system.model.Reservation;
import linx7a.reservation_system.model.ReservationStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class ReservationService {
    private final Map<Long, Reservation> reservationMap = Map.of(
            1L, new Reservation(1L, 100L, 40L, LocalDate.now(), LocalDate.now().plusDays(5), ReservationStatus.APPROVED),
            2L, new Reservation(2L, 101L, 41L, LocalDate.now(), LocalDate.now().plusDays(6), ReservationStatus.PENDING),
            3L, new Reservation(3L, 102L, 42L, LocalDate.now(), LocalDate.now().plusDays(2), ReservationStatus.CANCELLED)
    );

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
}
