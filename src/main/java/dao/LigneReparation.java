package dao;

import javax.persistence.*;

import dao.enums.EtatAppareil;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class LigneReparation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Plusieurs lignes pour une même réparation
    @ManyToOne
    @JoinColumn(nullable = false)
    private Reparation reparation;

    // Chaque ligne pointe vers un appareil
    @ManyToOne
    @JoinColumn(nullable = false)
    private Appareil appareil;

    // Etat de cet appareil dans cette réparation
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EtatAppareil etatAppareil;

    private String commentaire;

    private Double coutAppareil;
}
