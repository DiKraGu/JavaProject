package dao;

import javax.persistence.*;

import dao.enums.Role;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users") // "user" parfois réservé, donc "users" est plus sûr
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String motDePasse;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Nullable si PROPRIETAIRE
    private Double pourcentage;

    // Nullable si PROPRIETAIRE
    @ManyToOne
    private Boutique boutique;
}
