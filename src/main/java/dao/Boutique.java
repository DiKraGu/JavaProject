package dao;

import java.util.List;
import javax.persistence.*;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Boutique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String adresse;

    @Column(nullable = false, unique = true)
    private String numeroPatente;

    // Propriétaire de la boutique
    @ManyToOne
    @JoinColumn(nullable = false)
    private User proprietaire;

    // Liste des réparateurs de cette boutique (facultatif mais pratique)
    @OneToMany(mappedBy = "boutique", fetch = FetchType.LAZY)
    private List<User> reparateurs;
    
    @Override
    public String toString() {
        return id + " | " + nom + " | " + adresse;
    }

}
