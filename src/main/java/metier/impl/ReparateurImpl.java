package metier.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import dao.Appareil;
import dao.Boutique;
import dao.Client;
import dao.LigneReparation;
import dao.Reparation;
import dao.Transaction;
import dao.User;
import dao.enums.EtatAppareil;
import dao.enums.StatutReparation;
import dao.enums.TypeOperation;
import exception.MetierException;
import exception.NotFoundException;
import metier.impl.persistence.JpaUtil;
import metier.interfaces.IReparateur;

public class ReparateurImpl implements IReparateur {

    private final EntityManager em;

    public ReparateurImpl() {
        this.em = JpaUtil.getEntityManager();
    }

    // ===================== CLIENT =====================

    @Override
    public Client ajouterClient(Client c) {
        if (c == null) throw new MetierException("Client invalide");
        if (isBlank(c.getNom()) || isBlank(c.getPrenom()) || isBlank(c.getTelephone())) {
            throw new MetierException("Nom, prénom et téléphone sont obligatoires");
        }

        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(c);
            tr.commit();
            return c;
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur ajout client : " + e.getMessage());
        }
    }

    @Override
    public Client modifierClient(Client c) {
        if (c == null || c.getId() == null) throw new MetierException("Client invalide");
        if (isBlank(c.getNom()) || isBlank(c.getPrenom()) || isBlank(c.getTelephone())) {
            throw new MetierException("Nom, prénom et téléphone sont obligatoires");
        }

        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            Client res = em.merge(c);
            tr.commit();
            return res;
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur modification client : " + e.getMessage());
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
            throw new MetierException("Erreur suppression client : " + e.getMessage());
        }
    }

    @Override
    public Client rechercherClient(Long idClient) {
        if (idClient == null) throw new MetierException("Id client obligatoire");
        Client c = em.find(Client.class, idClient);
        if (c == null) throw new NotFoundException("Client introuvable");
        return c;
    }

    @Override
    public List<Client> listerClients() {
        TypedQuery<Client> q = em.createQuery("SELECT c FROM Client c ORDER BY c.id DESC", Client.class);
        return q.getResultList();
    }

    // ===================== APPAREIL =====================

    @Override
    public Appareil ajouterAppareil(Appareil a) {
        if (a == null) throw new MetierException("Appareil invalide");
        if (isBlank(a.getType()) || isBlank(a.getMarque()) || isBlank(a.getModele())) {
            throw new MetierException("Type, marque et modèle sont obligatoires");
        }

        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(a);
            tr.commit();
            return a;
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur ajout appareil : " + e.getMessage());
        }
    }

    @Override
    public Appareil modifierAppareil(Appareil a) {
        if (a == null || a.getId() == null) throw new MetierException("Appareil invalide");
        if (isBlank(a.getType()) || isBlank(a.getMarque()) || isBlank(a.getModele())) {
            throw new MetierException("Type, marque et modèle sont obligatoires");
        }

        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            Appareil res = em.merge(a);
            tr.commit();
            return res;
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur modification appareil : " + e.getMessage());
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
            throw new MetierException("Erreur suppression appareil : " + e.getMessage());
        }
    }

    @Override
    public Appareil rechercherAppareil(Long idAppareil) {
        if (idAppareil == null) throw new MetierException("Id appareil obligatoire");
        Appareil a = em.find(Appareil.class, idAppareil);
        if (a == null) throw new NotFoundException("Appareil introuvable");
        return a;
    }

    @Override
    public List<Appareil> listerAppareils() {
        TypedQuery<Appareil> q = em.createQuery("SELECT a FROM Appareil a ORDER BY a.id DESC", Appareil.class);
        return q.getResultList();
    }

    // ===================== REPARATION =====================

    @Override
    public Reparation creerReparation(Long idReparateur, Long idClient, Long idBoutique,
                                     String descriptionPanne,
                                     List<LigneReparation> lignes) {

        if (isBlank(descriptionPanne)) throw new MetierException("Description panne obligatoire");
        if (lignes == null || lignes.isEmpty()) throw new MetierException("Ajoutez au moins un appareil");
        if (idReparateur == null) throw new MetierException("Id réparateur obligatoire");
        if (idClient == null) throw new MetierException("Id client obligatoire");
        if (idBoutique == null) throw new MetierException("Id boutique obligatoire");

        User reparateur = em.find(User.class, idReparateur);
        if (reparateur == null) throw new NotFoundException("Réparateur introuvable");

        Client client = rechercherClient(idClient);

        Boutique boutique = em.find(Boutique.class, idBoutique);
        if (boutique == null) throw new NotFoundException("Boutique introuvable");

        // validation lignes + calcul coût
        double total = 0.0;
        for (LigneReparation lr : lignes) {
            if (lr == null) throw new MetierException("Ligne réparation invalide");
            if (lr.getAppareil() == null || lr.getAppareil().getId() == null) {
                throw new MetierException("Chaque ligne doit référencer un appareil existant (id non null)");
            }
            if (lr.getCoutAppareil() == null || lr.getCoutAppareil() < 0) {
                throw new MetierException("Coût appareil invalide (>=0) dans une ligne");
            }
            total += lr.getCoutAppareil();

            if (lr.getEtatAppareil() == null) {
                lr.setEtatAppareil(EtatAppareil.EN_COURS);
            }
        }

        String code = "REP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Reparation r = Reparation.builder()
                .codeSuivi(code)
                .descriptionPanne(descriptionPanne.trim())
                .coutTotal(total)
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

            for (LigneReparation lr : lignes) {
                // rattacher un appareil MANAGED
                Appareil appManaged = em.find(Appareil.class, lr.getAppareil().getId());
                if (appManaged == null) {
                    throw new NotFoundException("Appareil introuvable (id=" + lr.getAppareil().getId() + ")");
                }

                lr.setReparation(r);
                lr.setAppareil(appManaged);
                em.persist(lr);
            }

            // recalcul sécurisé depuis la BD
            recalculerCoutTotal(r.getId());

            tr.commit();

            return rechercherReparation(r.getId());

        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur création réparation : " + e.getMessage());
        }
    }

    @Override
    public Reparation modifierReparation(Long idReparation, String descriptionPanne) {
        if (idReparation == null) throw new MetierException("Id réparation obligatoire");
        if (isBlank(descriptionPanne)) throw new MetierException("Description panne obligatoire");

        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            Reparation r = rechercherReparation(idReparation);
            r.setDescriptionPanne(descriptionPanne.trim());
            Reparation res = em.merge(r);
            tr.commit();
            return res;
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur modification réparation : " + e.getMessage());
        }
    }

    @Override
    public void supprimerReparation(Long idReparation) {
        if (idReparation == null) throw new MetierException("Id réparation obligatoire");

        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            Reparation r = rechercherReparation(idReparation);
            em.remove(r); // orphanRemoval supprime aussi les lignes
            tr.commit();
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur suppression réparation : " + e.getMessage());
        }
    }

@Override
public Reparation changerStatutReparation(Long idReparation, String nouveauStatut) {
    if (idReparation == null) throw new MetierException("Id réparation obligatoire");

    StatutReparation nouveau;
    try {
        nouveau = StatutReparation.valueOf(nouveauStatut);
    } catch (Exception e) {
        throw new MetierException("Statut invalide");
    }

    EntityTransaction tr = em.getTransaction();
    try {
        tr.begin();

        Reparation r = rechercherReparation(idReparation);

        StatutReparation ancien = r.getStatut();
        if (ancien == null) ancien = StatutReparation.EN_COURS;

        // Si rien n'a changé -> rien à faire
        if (ancien == nouveau) {
            tr.commit();
            return r;
        }

        // MAJ statut
        r.setStatut(nouveau);
        Reparation res = em.merge(r);

        // Si on passe à TERMINE => créer automatiquement une transaction ENTREE (paiement)
        if (nouveau == StatutReparation.TERMINEE) {
            creerTransactionPaiementSiAbsente(res);
        }

        tr.commit();
        return res;

    } catch (Exception e) {
        if (tr.isActive()) tr.rollback();
        throw new MetierException("Erreur changement statut réparation : " + e.getMessage());
    }
}


    @Override
    public Reparation rechercherReparation(Long idReparation) {
        if (idReparation == null) throw new MetierException("Id réparation obligatoire");
        Reparation r = em.find(Reparation.class, idReparation);
        if (r == null) throw new NotFoundException("Réparation introuvable");
        return r;
    }

    @Override
    public List<Reparation> listerReparationsParReparateur(Long idReparateur) {
        if (idReparateur == null) throw new MetierException("Id réparateur obligatoire");

        TypedQuery<Reparation> q = em.createQuery(
                "SELECT r FROM Reparation r WHERE r.reparateur.id = :id ORDER BY r.dateCreation DESC",
                Reparation.class);
        q.setParameter("id", idReparateur);
        return q.getResultList();
    }

    // ===================== LIGNES =====================

    @Override
    public List<LigneReparation> listerLignesParReparation(Long idReparation) {
        if (idReparation == null) throw new MetierException("Id réparation obligatoire");

        TypedQuery<LigneReparation> q = em.createQuery(
                "SELECT lr FROM LigneReparation lr WHERE lr.reparation.id = :id ORDER BY lr.id DESC",
                LigneReparation.class);
        q.setParameter("id", idReparation);
        return q.getResultList();
    }

    @Override
    public LigneReparation ajouterLigne(Long idReparation, Long idAppareil, Double coutAppareil, String commentaire) {
        if (idReparation == null) throw new MetierException("Id réparation obligatoire");
        if (idAppareil == null) throw new MetierException("Id appareil obligatoire");
        if (coutAppareil == null || coutAppareil < 0) throw new MetierException("Coût appareil invalide (>=0)");

        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();

            Reparation rep = rechercherReparation(idReparation);
            Appareil app = rechercherAppareil(idAppareil);

            LigneReparation lr = LigneReparation.builder()
                    .reparation(rep)
                    .appareil(app)
                    .etatAppareil(EtatAppareil.EN_COURS)
                    .coutAppareil(coutAppareil)
                    .commentaire(commentaire)
                    .build();

            em.persist(lr);

            recalculerCoutTotal(idReparation);

            tr.commit();
            return lr;

        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur ajout ligne : " + e.getMessage());
        }
    }

    @Override
    public void supprimerLigne(Long idLigne) {
        if (idLigne == null) throw new MetierException("Id ligne obligatoire");

        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();

            LigneReparation lr = em.find(LigneReparation.class, idLigne);
            if (lr == null) throw new NotFoundException("Ligne introuvable");

            Long idRep = lr.getReparation().getId();
            em.remove(lr);

            recalculerCoutTotal(idRep);

            tr.commit();

        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur suppression ligne : " + e.getMessage());
        }
    }

    @Override
    public LigneReparation changerEtatLigne(Long idLigne, String nouvelEtat) {
        if (idLigne == null) throw new MetierException("Id ligne obligatoire");

        EtatAppareil etat;
        try {
            etat = EtatAppareil.valueOf(nouvelEtat);
        } catch (Exception e) {
            throw new MetierException("Etat appareil invalide");
        }

        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();

            LigneReparation lr = em.find(LigneReparation.class, idLigne);
            if (lr == null) throw new NotFoundException("Ligne introuvable");

            lr.setEtatAppareil(etat);
            LigneReparation res = em.merge(lr);

            tr.commit();
            return res;

        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur changement état ligne : " + e.getMessage());
        }
    }

    @Override
    public LigneReparation modifierLigne(Long idLigne, Double coutAppareil, String commentaire) {
        if (idLigne == null) throw new MetierException("Id ligne obligatoire");
        if (coutAppareil == null || coutAppareil < 0) throw new MetierException("Coût appareil invalide (>=0)");

        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();

            LigneReparation lr = em.find(LigneReparation.class, idLigne);
            if (lr == null) throw new NotFoundException("Ligne introuvable");

            lr.setCoutAppareil(coutAppareil);
            lr.setCommentaire(commentaire);

            LigneReparation res = em.merge(lr);

            recalculerCoutTotal(lr.getReparation().getId());

            tr.commit();
            return res;

        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new MetierException("Erreur modification ligne : " + e.getMessage());
        }
    }

    // ===================== TRANSACTIONS / CAISSE =====================

    @Override
    public Transaction ajouterTransaction(Long idReparateur, Long idReparation, LocalDateTime date,
                                         Double montant, String typeOperation, String description) {

        if (idReparateur == null) throw new MetierException("Id réparateur obligatoire");
        if (montant == null || montant <= 0) throw new MetierException("Montant invalide (>0)");

        User reparateur = em.find(User.class, idReparateur);
        if (reparateur == null) throw new NotFoundException("Réparateur introuvable");

        Reparation rep = null;
        if (idReparation != null) {
            rep = em.find(Reparation.class, idReparation);
            if (rep == null) throw new NotFoundException("Réparation introuvable");
        }

        TypeOperation op;
        try {
            op = TypeOperation.valueOf(typeOperation);
        } catch (Exception e) {
            throw new MetierException("Type opération invalide");
        }

        Transaction t = Transaction.builder()
                .date(date != null ? date : LocalDateTime.now())
                .montant(montant)
                .typeOperation(op)
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
            throw new MetierException("Erreur ajout transaction : " + e.getMessage());
        }
    }

    @Override
    public List<Transaction> listerTransactions(Long idReparateur, LocalDateTime debut, LocalDateTime fin) {
        if (idReparateur == null) throw new MetierException("Id réparateur obligatoire");
        if (debut == null || fin == null) throw new MetierException("Début et fin obligatoires");

        TypedQuery<Transaction> q = em.createQuery(
                "SELECT t FROM Transaction t WHERE t.reparateur.id = :id " +
                        "AND t.date BETWEEN :d1 AND :d2 " +
                        "ORDER BY t.date DESC",
                Transaction.class);

        q.setParameter("id", idReparateur);
        q.setParameter("d1", debut);
        q.setParameter("d2", fin);

        return q.getResultList();
    }

    @Override
    public Double totalEntrees(Long idReparateur, LocalDateTime debut, LocalDateTime fin) {
        return sommeMontant(idReparateur, TypeOperation.ENTREE, debut, fin);
    }

    @Override
    public Double totalSorties(Long idReparateur, LocalDateTime debut, LocalDateTime fin) {
        return sommeMontant(idReparateur, TypeOperation.SORTIE, debut, fin);
    }

    @Override
    public Double solde(Long idReparateur, LocalDateTime debut, LocalDateTime fin) {
        return totalEntrees(idReparateur, debut, fin) - totalSorties(idReparateur, debut, fin);
    }

    private Double sommeMontant(Long idReparateur, TypeOperation op,
                               LocalDateTime debut, LocalDateTime fin) {

        if (idReparateur == null) throw new MetierException("Id réparateur obligatoire");
        if (debut == null || fin == null) throw new MetierException("Début et fin obligatoires");

        TypedQuery<Double> q = em.createQuery(
                "SELECT COALESCE(SUM(t.montant), 0) FROM Transaction t " +
                        "WHERE t.reparateur.id = :id AND t.typeOperation = :op " +
                        "AND t.date BETWEEN :d1 AND :d2",
                Double.class);

        q.setParameter("id", idReparateur);
        q.setParameter("op", op);
        q.setParameter("d1", debut);
        q.setParameter("d2", fin);

        return q.getSingleResult();
    }

    // ===================== UTILS =====================

    private void recalculerCoutTotal(Long idReparation) {
        TypedQuery<Double> q = em.createQuery(
                "SELECT COALESCE(SUM(lr.coutAppareil), 0) FROM LigneReparation lr WHERE lr.reparation.id = :id",
                Double.class);
        q.setParameter("id", idReparation);
        Double total = q.getSingleResult();

        Reparation r = em.find(Reparation.class, idReparation);
        if (r == null) throw new NotFoundException("Réparation introuvable");

        r.setCoutTotal(total);
        em.merge(r);
    }
    
    private void creerTransactionPaiementSiAbsente(Reparation rep) {
        if (rep == null || rep.getId() == null) throw new MetierException("Réparation invalide");

        // Anti-doublon : existe déjà une ENTREE liée à cette réparation ?
        Long count = em.createQuery(
                "SELECT COUNT(t) FROM Transaction t " +
                "WHERE t.reparation.id = :rid AND t.typeOperation = :op",
                Long.class)
            .setParameter("rid", rep.getId())
            .setParameter("op", TypeOperation.ENTREE)
            .getSingleResult();

        if (count != null && count > 0) {
            return; // déjà payé (ou déjà enregistré), on ne recrée pas
        }

        // Montant = coutTotal de la réparation (si null -> 0 interdit)
        Double montant = rep.getCoutTotal();
        if (montant == null || montant <= 0) {
            throw new MetierException("Impossible d'enregistrer le paiement: coût total invalide");
        }

        Transaction t = Transaction.builder()
                .date(LocalDateTime.now())
                .montant(montant)
                .typeOperation(TypeOperation.ENTREE)
                .description("Paiement automatique - Réparation " + rep.getCodeSuivi())
                .reparateur(rep.getReparateur())
                .reparation(rep)
                .build();

        em.persist(t);
    }


    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
