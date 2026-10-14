package com.example;

import com.example.model.Reservation;
import com.example.service.ReservationService;
import javax.persistence.OptimisticLockException;

public class ReservationRetryHandler {

    private static final int MAX_RETRIES = 3;
    private final ReservationService service;

    public ReservationRetryHandler(ReservationService service) {
        this.service = service;
    }

    public Reservation modifierAvecRetry(Reservation reservation) {
        int tentative = 0;
        String nouvelleSalle = reservation.getSalle();
        while (tentative < MAX_RETRIES) {
            try {
                return service.modifierReservation(reservation);
            } catch (OptimisticLockException e) {
                tentative++;
                System.out.println("Conflit détecté, tentative de retry " + tentative);
                reservation = service.trouverParId(reservation.getId());
                reservation.setSalle(nouvelleSalle);
            }
        }
        throw new OptimisticLockException("Échec après " + MAX_RETRIES + " tentatives");
    }
}
