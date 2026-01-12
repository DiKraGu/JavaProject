package metier.interfaces;

import java.time.LocalDateTime;
import java.util.List;

import dao.Boutique;
import dao.Transaction;
import dao.User;

public interface IProprietaire {

    // ========= Boutiques (CRUD) =========
    Boutique creerBoutique(Boutique b);
    Boutique modifierBoutique(Boutique b);
    void supprimerBoutique(Long idBoutique);
    List<Boutique> listerBoutiquesDuProprietaire(Long idProprietaire);
    Boutique getBoutiqueDuProprietaire(Long idProprietaire, Long idBoutique);

    // ========= Réparateurs =========
    User creerReparateur(User reparateur, Long idBoutique);
    List<User> listerReparateursParBoutique(Long idBoutique);
    User modifierReparateur(User reparateur, Long idBoutique);
    void supprimerReparateur(Long idReparateur);

    // ========= Transactions / soldes =========
    List<Transaction> listerTransactionsReparateur(Long idReparateur, LocalDateTime debut, LocalDateTime fin);

    Double totalEntrees(Long idReparateur, LocalDateTime debut, LocalDateTime fin);
    Double totalSorties(Long idReparateur, LocalDateTime debut, LocalDateTime fin);
    Double solde(Long idReparateur, LocalDateTime debut, LocalDateTime fin);

    // ========= Profits =========
    Double profitProprietaireParBoutique(Long idBoutique, LocalDateTime debut, LocalDateTime fin);
    List<Object[]> profitProprietaireParReparateur(Long idBoutique, LocalDateTime debut, LocalDateTime fin);
}
