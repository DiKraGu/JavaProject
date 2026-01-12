package dao;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.*;

import dao.enums.StatutReparation;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Reparation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codeSuivi;

    @Column(nullable = false, length = 1000)
    private String descriptionPanne;

    @Column(nullable = false)
    private Double coutTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutReparation statut;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User reparateur;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Boutique boutique;

    // Réparation contient plusieurs appareils via LigneReparation
    @OneToMany(mappedBy = "reparation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LigneReparation> lignes;
    
    @Override
    public String toString() {
        return codeSuivi + " | " + (client != null ? (client.getNom() + " " + client.getPrenom()) : "");
    }

}
