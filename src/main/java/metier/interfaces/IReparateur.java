package metier.interfaces;

import java.time.LocalDateTime;
import java.util.List;

import dao.Appareil;
import dao.Client;
import dao.LigneReparation;
import dao.Reparation;
import dao.Transaction;

public interface IReparateur {

    // ================= CLIENT =================
    Client ajouterClient(Client c);
    Client modifierClient(Client c);
    void supprimerClient(Long idClient);
    Client rechercherClient(Long idClient);
    List<Client> listerClients();

    // ================= APPAREIL =================
    Appareil ajouterAppareil(Appareil a);
    Appareil modifierAppareil(Appareil a);
    void supprimerAppareil(Long idAppareil);
    Appareil rechercherAppareil(Long idAppareil);
    List<Appareil> listerAppareils();

    // ================= REPARATION =================
    Reparation creerReparation(Long idReparateur, Long idClient, Long idBoutique,
                               String descriptionPanne,
                               List<LigneReparation> lignes); // coutTotal calculé automatiquement

    
    Reparation modifierReparation(Long idReparation, String descriptionPanne); // simple

    void supprimerReparation(Long idReparation);

    Reparation changerStatutReparation(Long idReparation, String nouveauStatut);

    Reparation rechercherReparation(Long idReparation);

    List<Reparation> listerReparationsParReparateur(Long idReparateur);

    // ================= LIGNES =================
    List<LigneReparation> listerLignesParReparation(Long idReparation);

    LigneReparation ajouterLigne(Long idReparation, Long idAppareil, Double coutAppareil, String commentaire);

    void supprimerLigne(Long idLigne);

    LigneReparation changerEtatLigne(Long idLigne, String nouvelEtat);

    LigneReparation modifierLigne(Long idLigne, Double coutAppareil, String commentaire);

    // ================= CAISSE (Option C) =================
    Transaction ajouterTransaction(Long idReparateur, Long idReparation, LocalDateTime date,
                                   Double montant, String typeOperation, String typeCaisse, String description);

    List<Transaction> listerTransactions(Long idReparateur, LocalDateTime debut, LocalDateTime fin);

    Double soldeTempsReel(Long idReparateur, LocalDateTime debut, LocalDateTime fin);

    Double soldeReparation(Long idReparateur, LocalDateTime debut, LocalDateTime fin);
}
