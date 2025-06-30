package com.bsoft.ov8.loader.database;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nl.overheid.omgevingswet.ozon.model.LocatieType;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = {"identificatie", "geometrieIdentificatie"})
@ToString(exclude = {"regelingsgebieden"})
@Entity
@Table(name = "locatie", schema = "public", catalog = "ov8")
public class LocatieDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "identificatie")
    private String identificatie;

    @Column(name = "geometrieidentificatie")
    private String geometrieIdentificatie;

    @Enumerated(EnumType.STRING)
    @Column(name = "locatietype")
    private LocatieType locatieType;

    @Column(name = "noemer")
    private String noemer;

    @Embedded
    private BoundingBoxDTO boundingBox;

    @Embedded
    private RegistratiegegevensDTO registratiegegevens;

    // This is the inverse side of the Many-to-Many relationship
    @ManyToMany(mappedBy = "regelingsgebied") //, fetch = FetchType.LAZY)
    private Set<RegelingDTO> regelingsgebieden = new HashSet<>(); // Naming convention `regelingsgebieds` for the collection

    // Helper methods for managing the relationship (optional but good practice)
    public void addRegeling(RegelingDTO regeling) {
        if (this.regelingsgebieden == null) {
            this.regelingsgebieden = new HashSet<>();
        }
        if (regeling != null && !this.regelingsgebieden.contains(regeling)) {
            this.regelingsgebieden.add(regeling);
            regeling.getRegelingsgebied().add(this); // Maintain bidirectional relationship
        }
    }

    public void removeRegeling(RegelingDTO regeling) {
        if (this.regelingsgebieden != null) {
            this.regelingsgebieden.remove(regeling);
            regeling.getRegelingsgebied().remove(this); // Maintain bidirectional relationship
        }
    }
}
