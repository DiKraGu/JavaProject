package metier.interfaces;

import java.time.LocalDateTime;
import java.util.List;

import dao.Appareil;
import dao.Client;
import dao.LigneReparation;
import dao.Reparation;
import dao.Transaction;

public interface IReparateur {

    // ----- Client -----
    Client ajouterClient(Client c);
    Client modifierClient(Client c);
    void supprimerClient(Long idClient);
    Client rechercherClient(Long idClient);
    List<Client> listerClients();

    // ----- Appareil -----
    Appareil ajouterAppareil(Appareil a);
    Appareil modifierAppareil(Appareil a);
    void supprimerAppareil(Long idAppareil);
    Appareil rechercherAppareil(Long idAppareil);
    List<Appareil> listerAppareils();

    // ----- Réparation (avec plusieurs appareils via lignes) -----
    Reparation creerReparation(Long idReparateur, Long idClient, Long idBoutique,
                               String descriptionPanne, Double coutTotal,
                               List<LigneReparation> lignes);

    Reparation changerStatutReparation(Long idReparation, String nouveauStatut);

    Reparation rechercherReparation(Long idReparation);
    List<Reparation> listerReparationsParReparateur(Long idReparateur);

    // ----- Transactions / Caisse (Option C) -----
    Transaction ajouterTransaction(Long idReparateur, Long idReparation, LocalDateTime date,
                                   Double montant, String typeOperation, String typeCaisse, String description);

    List<Transaction> listerTransactions(Long idReparateur, LocalDateTime debut, LocalDateTime fin);

    Double soldeTempsReel(Long idReparateur, LocalDateTime debut, LocalDateTime fin);

    Double soldeReparation(Long idReparateur, LocalDateTime debut, LocalDateTime fin);
}
