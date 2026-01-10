package presentation;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import dao.Boutique;
import dao.User;
import dao.enums.Role;
import metier.impl.persistence.JpaUtil;

public class MainTest {

    public static void main(String[] args) {

        System.out.println("=== DÉMARRAGE TEST JPA ===");

        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();

        try {
            tr.begin();

            // 1) Création propriétaire
            User proprietaire = User.builder()
                    .email("proprio@test.com")
                    .motDePasse("1234")
                    .nom("PROPRIETAIRE")
                    .prenom("Ali")
                    .telephone("0600000000")
                    .role(Role.PROPRIETAIRE)
                    .build();

            em.persist(proprietaire);

            // 2) Création boutique
            Boutique boutique = Boutique.builder()
                    .nom("Boutique Centre")
                    .adresse("Derb Ghallef")
                    .numeroPatente("PAT-001")
                    .proprietaire(proprietaire)
                    .build();

            em.persist(boutique);

            // 3) Création réparateur
            User reparateur = User.builder()
                    .email("rep@test.com")
                    .motDePasse("1234")
                    .nom("REPARATEUR")
                    .prenom("Yassine")
                    .telephone("0611111111")
                    .role(Role.REPARATEUR)
                    .pourcentage(60.0)
                    .boutique(boutique)
                    .build();

            em.persist(reparateur);

            tr.commit();

            System.out.println("✔ Insertion OK");
            System.out.println("Propriétaire ID = " + proprietaire.getId());
            System.out.println("Boutique ID = " + boutique.getId());
            System.out.println("Réparateur ID = " + reparateur.getId());

        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            System.err.println("❌ ERREUR");
            e.printStackTrace();
        } finally {
            em.close();
            JpaUtil.close();
        }

        System.out.println("=== FIN TEST ===");
    }
}
