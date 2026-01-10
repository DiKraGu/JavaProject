package metier.interfaces;

import java.time.LocalDateTime;
import java.util.List;

import dao.Boutique;
import dao.Transaction;
import dao.User;

public interface IProprietaire {

    Boutique creerBoutique(Boutique b);

    User creerReparateur(User reparateur, Long idBoutique);

    List<User> listerReparateursParBoutique(Long idBoutique);

    List<Transaction> listerTransactionsReparateur(Long idReparateur, LocalDateTime debut, LocalDateTime fin);

    Double soldeTempsReel(Long idReparateur, LocalDateTime debut, LocalDateTime fin);

    Double soldeReparation(Long idReparateur, LocalDateTime debut, LocalDateTime fin);

    // Profit propriétaire sur une période (toutes réparations de la boutique)
    Double profitProprietaireParBoutique(Long idBoutique, LocalDateTime debut, LocalDateTime fin);
}
