package dao;

import java.time.LocalDateTime;
import javax.persistence.*;

import dao.enums.TypeOperation;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private Double montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeOperation typeOperation;

    @Column(length = 500)
    private String description;

    // Qui a fait la transaction (réparateur)
    @ManyToOne
    @JoinColumn(nullable = false)
    private User reparateur;

    // Optionnel: transaction liée à une réparation (traçabilité)
    @ManyToOne
    private Reparation reparation;
}
