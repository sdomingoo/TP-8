package com.example;

import com.example.model.Reservation;
import com.example.service.ReservationService;
import com.example.service.ReservationServiceImpl;

import javax.persistence.OptimisticLockException;
import java.time.LocalDateTime;

public class App {

    public static void main(String[] args) {
        ReservationService service = new ReservationServiceImpl();
        ReservationRetryHandler retryHandler = new ReservationRetryHandler(service);

        System.out.println("=== 1. Simulation sans retry (Conflit Optimiste) ===");
        Reservation r1 = new Reservation("Salle A", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        r1 = service.creerReservation(r1);

        Reservation tx1 = service.trouverParId(r1.getId());
        Reservation tx2 = service.trouverParId(r1.getId());

        tx1.setSalle("Salle B");
        service.modifierReservation(tx1);
        System.out.println("Transaction 1 : modification réussie.");

        try {
            tx2.setSalle("Salle C");
            service.modifierReservation(tx2);
            System.out.println("Transaction 2 : modification réussie.");
        } catch (OptimisticLockException e) {
            System.err.println("Transaction 2 : conflit détecté - " + e.getMessage());
        }

        System.out.println("\n=== 2. Simulation avec retry (Résolution automatique) ===");
        Reservation r2 = new Reservation("Salle 101", LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(3));
        r2 = service.creerReservation(r2);

        Reservation sim1 = service.trouverParId(r2.getId());
        Reservation sim2 = service.trouverParId(r2.getId());

        sim1.setSalle("Salle 102");
        service.modifierReservation(sim1);
        System.out.println("Transaction 1 : modification réussie.");

        sim2.setSalle("Salle 103");
        try {
            Reservation finale = retryHandler.modifierAvecRetry(sim2);
            System.out.println("Transaction 2 : modification réussie après retry (Salle = " + finale.getSalle() + ", Version = " + finale.getVersion() + ").");
        } catch (Exception e) {
            System.err.println("Transaction 2 : échec après retries - " + e.getMessage());
        }

        service.fermer();
    }
}
