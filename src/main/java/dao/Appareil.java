package dao;

import javax.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Appareil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ex: Telephone, PC, Tablette...
    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String marque;

    @Column(nullable = false)
    private String modele;

    private String ram;
    private String stockage;
}
