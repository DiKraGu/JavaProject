package metier.impl;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import dao.Boutique;
import dao.Reparation;
import dao.Transaction;
import dao.User;
import dao.enums.Role;
import dao.enums.TypeCaisse;
import dao.enums.TypeOperation;
import exception.MetierException;
import exception.NotFoundException;
import metier.impl.persistence.JpaUtil;
import metier.interfaces.IProprietaire;

public class ProprietaireImpl implements IProprietaire {

    private EntityManager em;

    public ProprietaireImpl() {
        this.em = JpaUtil.getEntityManager();
    }

    @Override
    public Boutique creerBoutique(Boutique b) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(b);
            tr.commit();
            return b;
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur création boutique");
        }
    }

    @Override
    public User creerReparateur(User reparateur, Long idBoutique) {
        Boutique b = em.find(Boutique.class, idBoutique);
        if (b == null) throw new NotFoundException("Boutique introuvable");

        if (reparateur.getRole() != Role.REPARATEUR) {
            throw new MetierException("Le user doit avoir le rôle REPARATEUR");
        }
        if (reparateur.getPourcentage() == null) {
            throw new MetierException("Pourcentage obligatoire pour un réparateur");
        }

        reparateur.setBoutique(b);

        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(reparateur);
            tr.commit();
            return reparateur;
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur création réparateur");
        }
    }

    @Override
    public List<User> listerReparateursParBoutique(Long idBoutique) {
        TypedQuery<User> q = em.createQuery(
                "SELECT u FROM User u WHERE u.role = :r AND u.boutique.id = :id",
                User.class);
        q.setParameter("r", Role.REPARATEUR);
        q.setParameter("id", idBoutique);
        return q.getResultList();
    }

    @Override
    public List<Transaction> listerTransactionsReparateur(Long idReparateur, LocalDateTime debut, LocalDateTime fin) {
        TypedQuery<Transaction> q = em.createQuery(
                "SELECT t FROM Transaction t WHERE t.reparateur.id = :id AND t.date BETWEEN :d1 AND :d2 ORDER BY t.date DESC",
                Transaction.class);
        q.setParameter("id", idReparateur);
        q.setParameter("d1", debut);
        q.setParameter("d2", fin);
        return q.getResultList();
    }

    @Override
    public Double soldeTempsReel(Long idReparateur, LocalDateTime debut, LocalDateTime fin) {
        return calculerSolde(idReparateur, TypeCaisse.TEMPS_REEL, debut, fin);
    }

    @Override
    public Double soldeReparation(Long idReparateur, LocalDateTime debut, LocalDateTime fin) {
        return calculerSolde(idReparateur, TypeCaisse.REPARATION, debut, fin);
    }

    private Double calculerSolde(Long idReparateur, TypeCaisse typeCaisse, LocalDateTime debut, LocalDateTime fin) {
        Double entree = sommeMontant(idReparateur, typeCaisse, TypeOperation.ENTREE, debut, fin);
        Double sortie = sommeMontant(idReparateur, typeCaisse, TypeOperation.SORTIE, debut, fin);
        return entree - sortie;
    }

    private Double sommeMontant(Long idReparateur, TypeCaisse tc, TypeOperation op,
                               LocalDateTime debut, LocalDateTime fin) {
        TypedQuery<Double> q = em.createQuery(
                "SELECT COALESCE(SUM(t.montant), 0) FROM Transaction t "
                        + "WHERE t.reparateur.id = :id AND t.typeCaisse = :tc AND t.typeOperation = :op "
                        + "AND t.date BETWEEN :d1 AND :d2",
                Double.class);
        q.setParameter("id", idReparateur);
        q.setParameter("tc", tc);
        q.setParameter("op", op);
        q.setParameter("d1", debut);
        q.setParameter("d2", fin);
        return q.getSingleResult();
    }

    @Override
    public Double profitProprietaireParBoutique(Long idBoutique, LocalDateTime debut, LocalDateTime fin) {
        // Profit propriétaire = somme( coutTotal * (100 - pourcentage)/100 ) pour toutes réparations de la boutique
        // On fait simple: on récupère les réparations et on calcule en Java.
        TypedQuery<Reparation> q = em.createQuery(
                "SELECT r FROM Reparation r WHERE r.boutique.id = :id AND r.dateCreation BETWEEN :d1 AND :d2",
                Reparation.class);
        q.setParameter("id", idBoutique);
        q.setParameter("d1", debut);
        q.setParameter("d2", fin);

        List<Reparation> reps = q.getResultList();

        double total = 0.0;
        for (Reparation r : reps) {
            User rep = r.getReparateur();
            Double pct = rep.getPourcentage() != null ? rep.getPourcentage() : 0.0;
            total += r.getCoutTotal() * (100.0 - pct) / 100.0;
        }
        return total;
    }
}
