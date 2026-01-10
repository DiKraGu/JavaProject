package metier.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import dao.*;
import dao.enums.*;
import exception.MetierException;
import exception.NotFoundException;
import metier.impl.persistence.JpaUtil;
import metier.interfaces.IReparateur;

public class ReparateurImpl implements IReparateur {

    private EntityManager em;

    public ReparateurImpl() {
        this.em = JpaUtil.getEntityManager();
    }

    // ----------------- CLIENT -----------------
    @Override
    public Client ajouterClient(Client c) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(c);
            tr.commit();
            return c;
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur ajout client");
        }
    }

    @Override
    public Client modifierClient(Client c) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            Client res = em.merge(c);
            tr.commit();
            return res;
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur modification client");
        }
    }

    @Override
    public void supprimerClient(Long idClient) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            Client c = rechercherClient(idClient);
            em.remove(c);
            tr.commit();
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur suppression client");
        }
    }

    @Override
    public Client rechercherClient(Long idClient) {
        Client c = em.find(Client.class, idClient);
        if (c == null) throw new NotFoundException("Client introuvable");
        return c;
    }

    @Override
    public List<Client> listerClients() {
        TypedQuery<Client> q = em.createQuery("SELECT c FROM Client c", Client.class);
        return q.getResultList();
    }

    // ----------------- APPAREIL -----------------
    @Override
    public Appareil ajouterAppareil(Appareil a) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(a);
            tr.commit();
            return a;
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur ajout appareil");
        }
    }

    @Override
    public Appareil modifierAppareil(Appareil a) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            Appareil res = em.merge(a);
            tr.commit();
            return res;
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur modification appareil");
        }
    }

    @Override
    public void supprimerAppareil(Long idAppareil) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            Appareil a = rechercherAppareil(idAppareil);
            em.remove(a);
            tr.commit();
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur suppression appareil");
        }
    }

    @Override
    public Appareil rechercherAppareil(Long idAppareil) {
        Appareil a = em.find(Appareil.class, idAppareil);
        if (a == null) throw new NotFoundException("Appareil introuvable");
        return a;
    }

    @Override
    public List<Appareil> listerAppareils() {
        TypedQuery<Appareil> q = em.createQuery("SELECT a FROM Appareil a", Appareil.class);
        return q.getResultList();
    }

    // ----------------- REPARATION -----------------
    @Override
    public Reparation creerReparation(Long idReparateur, Long idClient, Long idBoutique,
                                     String descriptionPanne, Double coutTotal,
                                     List<LigneReparation> lignes) {

        if (coutTotal == null || coutTotal < 0) throw new MetierException("Coût invalide");
        if (lignes == null || lignes.isEmpty()) throw new MetierException("La réparation doit contenir au moins un appareil");

        User reparateur = em.find(User.class, idReparateur);
        if (reparateur == null) throw new NotFoundException("Réparateur introuvable");

        Client client = rechercherClient(idClient);

        Boutique boutique = em.find(Boutique.class, idBoutique);
        if (boutique == null) throw new NotFoundException("Boutique introuvable");

        // code unique simple
        String code = "REP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Reparation r = Reparation.builder()
                .codeSuivi(code)
                .descriptionPanne(descriptionPanne)
                .coutTotal(coutTotal)
                .statut(StatutReparation.EN_COURS)
                .dateCreation(LocalDateTime.now())
                .reparateur(reparateur)
                .client(client)
                .boutique(boutique)
                .build();

        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();

            em.persist(r);

            // lignes : il faut attacher la réparation à chaque ligne
            for (LigneReparation lr : lignes) {
                if (lr.getAppareil() == null) throw new MetierException("Appareil manquant dans une ligne");
                if (lr.getEtatAppareil() == null) lr.setEtatAppareil(EtatAppareil.EN_COURS);
                lr.setReparation(r);
                em.persist(lr);
            }

            tr.commit();
            return r;

        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur création réparation");
        }
    }

    @Override
    public Reparation changerStatutReparation(Long idReparation, String nouveauStatut) {
        EntityTransaction tr = em.getTransaction();
        try {
            Reparation r = rechercherReparation(idReparation);

            StatutReparation s;
            try {
                s = StatutReparation.valueOf(nouveauStatut);
            } catch (Exception e) {
                throw new MetierException("Statut invalide");
            }

            tr.begin();
            r.setStatut(s);
            Reparation res = em.merge(r);
            tr.commit();
            return res;

        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw e;
        }
    }

    @Override
    public Reparation rechercherReparation(Long idReparation) {
        Reparation r = em.find(Reparation.class, idReparation);
        if (r == null) throw new NotFoundException("Réparation introuvable");
        return r;
    }

    @Override
    public List<Reparation> listerReparationsParReparateur(Long idReparateur) {
        TypedQuery<Reparation> q = em.createQuery(
                "SELECT r FROM Reparation r WHERE r.reparateur.id = :id", Reparation.class);
        q.setParameter("id", idReparateur);
        return q.getResultList();
    }

    // ----------------- TRANSACTIONS / CAISSE (Option C) -----------------
    @Override
    public Transaction ajouterTransaction(Long idReparateur, Long idReparation, LocalDateTime date,
                                         Double montant, String typeOperation, String typeCaisse, String description) {

        if (montant == null || montant <= 0) throw new MetierException("Montant invalide");

        User reparateur = em.find(User.class, idReparateur);
        if (reparateur == null) throw new NotFoundException("Réparateur introuvable");

        Reparation rep = null;
        if (idReparation != null) {
            rep = em.find(Reparation.class, idReparation);
            if (rep == null) throw new NotFoundException("Réparation introuvable");
        }

        TypeOperation op;
        TypeCaisse tc;
        try {
            op = TypeOperation.valueOf(typeOperation);
            tc = TypeCaisse.valueOf(typeCaisse);
        } catch (Exception e) {
            throw new MetierException("Type opération ou type caisse invalide");
        }

        Transaction t = Transaction.builder()
                .date(date != null ? date : LocalDateTime.now())
                .montant(montant)
                .typeOperation(op)
                .typeCaisse(tc)
                .description(description)
                .reparateur(reparateur)
                .reparation(rep)
                .build();

        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(t);
            tr.commit();
            return t;
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur ajout transaction");
        }
    }

    @Override
    public List<Transaction> listerTransactions(Long idReparateur, LocalDateTime debut, LocalDateTime fin) {
        TypedQuery<Transaction> q = em.createQuery(
                "SELECT t FROM Transaction t WHERE t.reparateur.id = :id "
                        + "AND t.date BETWEEN :d1 AND :d2 "
                        + "ORDER BY t.date DESC",
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
        // solde = SUM(ENTREE) - SUM(SORTIE)
        Double entree = sommeMontant(idReparateur, typeCaisse, TypeOperation.ENTREE, debut, fin);
        Double sortie = sommeMontant(idReparateur, typeCaisse, TypeOperation.SORTIE, debut, fin);
        return (entree - sortie);
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
}
