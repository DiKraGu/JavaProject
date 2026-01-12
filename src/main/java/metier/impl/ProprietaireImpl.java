package metier.impl;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
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

    private final EntityManager em;

    public ProprietaireImpl() {
        this.em = JpaUtil.getEntityManager();
    }

    // =================== BOUTIQUE (CRUD) ===================

    @Override
    public Boutique creerBoutique(Boutique b) {
        if (b == null) throw new MetierException("Boutique invalide");
        if (isBlank(b.getNom()) || isBlank(b.getAdresse()) || isBlank(b.getNumeroPatente())) {
            throw new MetierException("Nom, adresse et numéro patente sont obligatoires");
        }
        if (b.getProprietaire() == null || b.getProprietaire().getId() == null) {
            throw new MetierException("Propriétaire obligatoire pour créer une boutique");
        }

        User prop = em.find(User.class, b.getProprietaire().getId());
        if (prop == null) throw new NotFoundException("Propriétaire introuvable");
        if (prop.getRole() != Role.PROPRIETAIRE) throw new MetierException("Le user doit être PROPRIETAIRE");

        b.setProprietaire(prop);

        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(b);
            tr.commit();
            return b;
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur création boutique : " + e.getMessage());
        }
    }

    @Override
    public Boutique modifierBoutique(Boutique b) {
        if (b == null || b.getId() == null) throw new MetierException("Boutique invalide");
        if (isBlank(b.getNom()) || isBlank(b.getAdresse()) || isBlank(b.getNumeroPatente())) {
            throw new MetierException("Nom, adresse et numéro patente sont obligatoires");
        }

        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();

            Boutique exist = em.find(Boutique.class, b.getId());
            if (exist == null) throw new NotFoundException("Boutique introuvable");

            // on modifie uniquement les champs simples
            exist.setNom(b.getNom().trim());
            exist.setAdresse(b.getAdresse().trim());
            exist.setNumeroPatente(b.getNumeroPatente().trim());

            Boutique res = em.merge(exist);
            tr.commit();
            return res;

        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new MetierException("Erreur modification boutique : " + e.getMessage());
        }
    }

    @Override
    public void supprimerBoutique(Long idBoutique) {
        if (idBoutique == null) throw new MetierException("Id boutique obligatoire");

        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            Boutique b = em.find(Boutique.class, idBoutique);
            if (b == null) throw new NotFoundException("Boutique introuvable");

            em.remove(b);
            tr.commit();

        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur suppression boutique : " + e.getMessage());
        }
    }

    @Override
    public List<Boutique> listerBoutiquesDuProprietaire(Long idProprietaire) {
        if (idProprietaire == null) throw new MetierException("Id propriétaire obligatoire");

        TypedQuery<Boutique> q = em.createQuery(
                "SELECT b FROM Boutique b WHERE b.proprietaire.id = :id ORDER BY b.id DESC",
                Boutique.class);
        q.setParameter("id", idProprietaire);
        return q.getResultList();
    }

    @Override
    public Boutique getBoutiqueDuProprietaire(Long idProprietaire, Long idBoutique) {
        if (idProprietaire == null) throw new MetierException("Id propriétaire obligatoire");
        if (idBoutique == null) throw new MetierException("Id boutique obligatoire");

        try {
            TypedQuery<Boutique> q = em.createQuery(
                    "SELECT b FROM Boutique b WHERE b.id = :bid AND b.proprietaire.id = :pid",
                    Boutique.class);
            q.setParameter("bid", idBoutique);
            q.setParameter("pid", idProprietaire);
            return q.getSingleResult();
        } catch (NoResultException e) {
            throw new NotFoundException("Boutique introuvable ou non autorisée pour ce propriétaire");
        }
    }

    // =================== REPARATEURS (CRUD) ===================

    @Override
    public User creerReparateur(User reparateur, Long idBoutique) {
        if (reparateur == null) throw new MetierException("Réparateur invalide");
        if (idBoutique == null) throw new MetierException("Id boutique obligatoire");

        Boutique b = em.find(Boutique.class, idBoutique);
        if (b == null) throw new NotFoundException("Boutique introuvable");

        if (reparateur.getRole() != Role.REPARATEUR) {
            throw new MetierException("Le user doit avoir le rôle REPARATEUR");
        }
        if (isBlank(reparateur.getEmail()) || isBlank(reparateur.getMotDePasse())
                || isBlank(reparateur.getNom()) || isBlank(reparateur.getPrenom())) {
            throw new MetierException("Email, mot de passe, nom, prénom obligatoires");
        }
        if (reparateur.getPourcentage() == null) {
            throw new MetierException("Pourcentage obligatoire pour un réparateur");
        }
        if (reparateur.getPourcentage() < 0 || reparateur.getPourcentage() > 100) {
            throw new MetierException("Pourcentage invalide (0..100)");
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
            throw new MetierException("Erreur création réparateur : " + e.getMessage());
        }
    }

    @Override
    public List<User> listerReparateursParBoutique(Long idBoutique) {
        if (idBoutique == null) throw new MetierException("Id boutique obligatoire");

        TypedQuery<User> q = em.createQuery(
                "SELECT u FROM User u WHERE u.role = :r AND u.boutique.id = :id ORDER BY u.id DESC",
                User.class);
        q.setParameter("r", Role.REPARATEUR);
        q.setParameter("id", idBoutique);
        return q.getResultList();
    }

    @Override
    public User modifierReparateur(User reparateur, Long idBoutique) {
        if (reparateur == null || reparateur.getId() == null) {
            throw new MetierException("Réparateur invalide (id obligatoire)");
        }
        if (idBoutique == null) throw new MetierException("Id boutique obligatoire");

        if (isBlank(reparateur.getEmail())
                || isBlank(reparateur.getNom())
                || isBlank(reparateur.getPrenom())) {
            throw new MetierException("Email, nom, prénom obligatoires");
        }

        if (reparateur.getPourcentage() == null || reparateur.getPourcentage() < 0 || reparateur.getPourcentage() > 100) {
            throw new MetierException("Pourcentage invalide (0..100)");
        }

        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();

            // Réparateur existant
            User exist = em.find(User.class, reparateur.getId());
            if (exist == null) throw new NotFoundException("Réparateur introuvable");
            if (exist.getRole() != Role.REPARATEUR) throw new MetierException("User non réparateur");

            // Boutique
            Boutique b = em.find(Boutique.class, idBoutique);
            if (b == null) throw new NotFoundException("Boutique introuvable");

            // MAJ champs
            exist.setEmail(reparateur.getEmail().trim());

            // mot de passe : si vide, on ne change pas
            if (!isBlank(reparateur.getMotDePasse())) {
                exist.setMotDePasse(reparateur.getMotDePasse().trim());
            }

            exist.setNom(reparateur.getNom().trim());
            exist.setPrenom(reparateur.getPrenom().trim());
            exist.setTelephone(reparateur.getTelephone() != null ? reparateur.getTelephone().trim() : null);
            exist.setPourcentage(reparateur.getPourcentage());
            exist.setBoutique(b);

            User res = em.merge(exist);
            tr.commit();
            return res;

        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new MetierException("Erreur modification réparateur : " + e.getMessage());
        }
    }


    @Override
    public void supprimerReparateur(Long idReparateur) {
        if (idReparateur == null) throw new MetierException("Id réparateur obligatoire");

        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();

            User u = em.find(User.class, idReparateur);
            if (u == null) throw new NotFoundException("Réparateur introuvable");
            if (u.getRole() != Role.REPARATEUR) throw new MetierException("User non réparateur");

            em.remove(u);
            tr.commit();

        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur suppression réparateur : " + e.getMessage());
        }
    }

    // =================== TRANSACTIONS / SOLDES ===================

    @Override
    public List<Transaction> listerTransactionsReparateur(Long idReparateur, LocalDateTime debut, LocalDateTime fin) {
        if (idReparateur == null) throw new MetierException("Id réparateur obligatoire");
        if (debut == null || fin == null) throw new MetierException("Début et fin obligatoires");

        TypedQuery<Transaction> q = em.createQuery(
                "SELECT t FROM Transaction t " +
                        "WHERE t.reparateur.id = :id AND t.date BETWEEN :d1 AND :d2 " +
                        "ORDER BY t.date DESC",
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
                "SELECT COALESCE(SUM(t.montant), 0) FROM Transaction t " +
                        "WHERE t.reparateur.id = :id AND t.typeCaisse = :tc AND t.typeOperation = :op " +
                        "AND t.date BETWEEN :d1 AND :d2",
                Double.class);
        q.setParameter("id", idReparateur);
        q.setParameter("tc", tc);
        q.setParameter("op", op);
        q.setParameter("d1", debut);
        q.setParameter("d2", fin);
        return q.getSingleResult();
    }

    // =================== PROFIT ===================

    @Override
    public Double profitProprietaireParBoutique(Long idBoutique, LocalDateTime debut, LocalDateTime fin) {
        if (idBoutique == null) throw new MetierException("Id boutique obligatoire");
        if (debut == null || fin == null) throw new MetierException("Début et fin obligatoires");

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
            Double pct = (rep.getPourcentage() != null) ? rep.getPourcentage() : 0.0;
            total += r.getCoutTotal() * (100.0 - pct) / 100.0;
        }
        return total;
    }

    @Override
    public List<Object[]> profitProprietaireParReparateur(Long idBoutique, LocalDateTime debut, LocalDateTime fin) {
        if (idBoutique == null) throw new MetierException("Id boutique obligatoire");
        if (debut == null || fin == null) throw new MetierException("Début et fin obligatoires");

        // Chaque ligne = 1 réparateur
        // ca = SUM(coutTotal)
        // profitProp = SUM(coutTotal*(100 - pct)/100)
        TypedQuery<Object[]> q = em.createQuery(
                "SELECT rep.id, rep.nom, rep.prenom, rep.pourcentage, " +
                        "COALESCE(SUM(r.coutTotal), 0), " +
                        "COALESCE(SUM(r.coutTotal * (100 - COALESCE(rep.pourcentage, 0)) / 100), 0) " +
                        "FROM Reparation r JOIN r.reparateur rep " +
                        "WHERE r.boutique.id = :id AND r.dateCreation BETWEEN :d1 AND :d2 " +
                        "GROUP BY rep.id, rep.nom, rep.prenom, rep.pourcentage " +
                        "ORDER BY rep.nom ASC",
                Object[].class);

        q.setParameter("id", idBoutique);
        q.setParameter("d1", debut);
        q.setParameter("d2", fin);

        return q.getResultList();
    }

    // =================== Utils ===================

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
