package com.example.service;

import com.example.model.Reservation;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.OptimisticLockException;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;
import java.util.List;

public class ReservationServiceImpl implements ReservationService {

    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("reservationPU");

    @Override
    public Reservation creerReservation(Reservation reservation) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(reservation);
            em.getTransaction().commit();
            return reservation;
        } finally {
            em.close();
        }
    }

    @Override
    public Reservation modifierReservation(Reservation reservation) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Reservation merged = em.merge(reservation);
            em.flush();
            em.getTransaction().commit();
            return merged;
        } catch (OptimisticLockException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } catch (RollbackException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (e.getCause() instanceof OptimisticLockException) {
                throw (OptimisticLockException) e.getCause();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Reservation trouverParId(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Reservation.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Reservation> listerReservations() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT r FROM Reservation r", Reservation.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void fermer() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
