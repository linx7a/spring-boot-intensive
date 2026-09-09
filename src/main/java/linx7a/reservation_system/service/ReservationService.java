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
//        if (reservationMap.isEmpty()) {
//            throw new RuntimeException("Ничего не найдено.");
//        }
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
}
