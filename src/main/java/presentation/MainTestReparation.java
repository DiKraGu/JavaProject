package presentation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import dao.*;
import dao.enums.*;
import metier.impl.ReparateurImpl;
import metier.impl.persistence.JpaUtil;

public class MainTestReparation {

    public static void main(String[] args) {

        System.out.println("=== TEST REPARATION + LIGNES + TRANSACTION ===");

        EntityManager em = JpaUtil.getEntityManager();

        try {
            // 1) Récupérer les IDs existants (créés dans le test précédent)
            // On suppose : propriétaire id=1, boutique id=1, réparateur id=2
            Long idBoutique = 1L;
            Long idReparateur = 2L;

            // Vérifier que réparateur et boutique existent
            User reparateur = em.find(User.class, idReparateur);
            Boutique boutique = em.find(Boutique.class, idBoutique);

            if (reparateur == null || boutique == null) {
                System.out.println("❌ Boutique ou Réparateur introuvable. Lance d'abord le 1er test (création users/boutique).");
                return;
            }

            System.out.println("✔ Boutique trouvée : " + boutique.getNom());
            System.out.println("✔ Réparateur trouvé : " + reparateur.getPrenom());

            // 2) Créer un client
            Client client = Client.builder()
                    .nom("CLIENT")
                    .prenom("Hajar")
                    .telephone("0622222222")
                    .build();

            // 3) Créer 2 appareils
            Appareil a1 = Appareil.builder()
                    .type("Telephone")
                    .marque("Apple")
                    .modele("iPhone 12")
                    .ram("4GB")
                    .stockage("128GB")
                    .build();

            Appareil a2 = Appareil.builder()
                    .type("PC")
                    .marque("Dell")
                    .modele("Inspiron 15")
                    .ram("8GB")
                    .stockage("256GB SSD")
                    .build();

            // Persist client + appareils en transaction
            EntityTransaction tr = em.getTransaction();
            try {
                tr.begin();
                em.persist(client);
                em.persist(a1);
                em.persist(a2);
                tr.commit();
            } catch (Exception e) {
                if (tr.isActive()) tr.rollback();
                throw e;
            }

            System.out.println("✔ Client créé ID = " + client.getId());
            System.out.println("✔ Appareil1 créé ID = " + a1.getId());
            System.out.println("✔ Appareil2 créé ID = " + a2.getId());

            // 4) Préparer les lignes de réparation (plusieurs appareils)
            List<LigneReparation> lignes = new ArrayList<>();

            LigneReparation lr1 = LigneReparation.builder()
                    .appareil(a1)
                    .etatAppareil(EtatAppareil.EN_COURS)
                    .commentaire("Changement écran")
                    .coutAppareil(400.0)
                    .build();

            LigneReparation lr2 = LigneReparation.builder()
                    .appareil(a2)
                    .etatAppareil(EtatAppareil.EN_COURS)
                    .commentaire("Problème alimentation")
                    .coutAppareil(300.0)
                    .build();

            lignes.add(lr1);
            lignes.add(lr2);

            // 5) Créer la réparation via le métier
            ReparateurImpl metier = new ReparateurImpl();

            Reparation rep = metier.creerReparation(
                    idReparateur,
                    client.getId(),
                    idBoutique,
                    "Pannes diverses (2 appareils)",
                    700.0,
                    lignes
            );

            System.out.println("✔ Réparation créée ID = " + rep.getId());
            System.out.println("✔ Code suivi = " + rep.getCodeSuivi());
            System.out.println("✔ Statut = " + rep.getStatut());

            // 6) Ajouter une transaction liée à la réparation (Option C)
            Transaction t = metier.ajouterTransaction(
                    idReparateur,
                    rep.getId(),
                    LocalDateTime.now(),
                    700.0,
                    "ENTREE",
                    "REPARATION",
                    "Paiement client réparation (2 appareils)"
            );

            System.out.println("✔ Transaction créée ID = " + t.getId()
                    + " | montant=" + t.getMontant()
                    + " | typeCaisse=" + t.getTypeCaisse());

            // 7) Lire et afficher la réparation + ses lignes (avec un nouvel EM pour vérifier)
            EntityManager em2 = JpaUtil.getEntityManager();
            Reparation repDb = em2.find(Reparation.class, rep.getId());

            System.out.println("\n=== AFFICHAGE REPARATION ===");
            System.out.println("Code : " + repDb.getCodeSuivi());
            System.out.println("Client : " + repDb.getClient().getNom() + " " + repDb.getClient().getPrenom());
            System.out.println("Coût total : " + repDb.getCoutTotal());
            System.out.println("Statut : " + repDb.getStatut());
            System.out.println("Nb appareils : " + repDb.getLignes().size());

            for (LigneReparation lr : repDb.getLignes()) {
                System.out.println(" - Appareil: " + lr.getAppareil().getType()
                        + " " + lr.getAppareil().getMarque()
                        + " " + lr.getAppareil().getModele()
                        + " | Etat: " + lr.getEtatAppareil()
                        + " | Cout: " + lr.getCoutAppareil()
                        + " | Commentaire: " + lr.getCommentaire());
            }

            em2.close();

            // 8) Tester calcul caisse (Option C) : solde REPARATION
            LocalDateTime debut = LocalDateTime.now().minusDays(1);
            LocalDateTime fin = LocalDateTime.now().plusDays(1);

            Double soldeReparation = metier.soldeReparation(idReparateur, debut, fin);
            Double soldeTempsReel = metier.soldeTempsReel(idReparateur, debut, fin);

            System.out.println("\n=== SOLDES (Option C) ===");
            System.out.println("Solde caisse REPARATION = " + soldeReparation);
            System.out.println("Solde caisse TEMPS_REEL = " + soldeTempsReel);

            System.out.println("\n✅ TEST COMPLET RÉUSSI");

        } catch (Exception e) {
            System.out.println("❌ ERREUR TEST");
            e.printStackTrace();
        } finally {
            em.close();
            JpaUtil.close();
        }

        System.out.println("=== FIN TEST ===");
    }
}
