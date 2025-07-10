package com.bsoft.ov8.loader.database;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"bevoegdGezag", "type", "opvolgerVan", "regelingsgebied"})
@Entity
@Table(name = "regeling", schema = "public", catalog = "ov8")
public class RegelingDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "identificatie")
    private String identificatie;

    @Column(name = "officieletitel")
    private String officieleTitel;

    //
    // aangeleverd door bevoegd gezag
    //
    // This is the "many" side of the Many-to-One relationship.
    // Each RegelingDTO has one BevoegdGezagDTO.
    // The foreign key column will be created in the 'regeling' table.
    @ManyToOne // Default fetch type is EAGER for ManyToOne, consider LAZY if performance is an issue
    @JoinColumn(name = "bevoegdgezag_id") // Specifies the foreign key column name in the RegelingDTO table
    private BevoegdGezagDTO bevoegdGezag; // Singular, as each Regeling has one BG

    /*
    @Column(name = "links")
    private RegelingLinks links;
    */

    // This is the "many" side of the Many-to-One relationship.
    // Each RegelingDTO has one BevoegdGezagDTO.
    // The foreign key column will be created in the 'regeling' table.
    @ManyToOne // Default fetch type is EAGER for ManyToOne, consider LAZY if performance is an issue
    @JoinColumn(name = "soortregeling_id") // Specifies the foreign key column name in the RegelingDTO table
    private SoortRegelingDTO type; // Singular, as each Regeling has one BG

    @Embedded
    private RegistratiegegevensDTO registratiegegevens;

    @Column(name = "citeertitel")
    private String citeerTitel;

    @Column(name = "opschrift")
    private String opschrift;

    @Column(name = "conditie")
    private String conditie;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "regeling_opvolgervan", // This will be the name of your join table
            joinColumns = @JoinColumn(name = "regeling_id"), // Column in the join table referring to THIS RegelingDTO
            inverseJoinColumns = @JoinColumn(name = "opvolgerregeling_id") // Column in the join table referring to the OTHER RegelingDTO (the one it succeeds)
    )
    // It's generally better to use Set for ManyToMany to avoid duplicate entries and for better performance
    private Set<RegelingDTO> opvolgerVan = new HashSet<>();

    @Column(name = "heeftbijlagen")
    private Boolean heeftBijlagen;

    @Column(name = "heefttoelichtingen")
    private Boolean heeftToelichtingen;

    @Column(name = "publicatieid")
    private String publicatieID;

    @Column(name = "inwerkingtot")
    private String inwerkingTot;

    @Column(name = "geldigtot")
    private String geldigTot;

    /*
    @Column(name = "embedded")
    private RegelingAllOfEmbedded embedded;
     */
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}) // Or CascadeType.ALL for broader cascading
    @JoinTable(
            name = "regeling_regelingsgebied", // This will be the name of your join table
            joinColumns = @JoinColumn(name = "regeling_id"), // Column in the join table referring to THIS RegelingDTO
            inverseJoinColumns = @JoinColumn(name = "locatie_id") // Column in the join table referring to the OTHER RegelingDTO (the one it succeeds)
    )
    private Set<LocatieDTO> regelingsgebied;

    // Helper methods to manage the relationship (optional but good practice)
    public void addOpvolgerVan(RegelingDTO opvolgerVan) {
        this.opvolgerVan.add(opvolgerVan);
        opvolgerVan.getOpvolgerVan().add(this); // Maintain the other side
    }

    public void removeOpvolgerVan(RegelingDTO opvolgerVan) {
        this.opvolgerVan.remove(opvolgerVan);
        opvolgerVan.getOpvolgerVan().remove(this); // Maintain the other side
    }

    /*
        public void addRegelingsgebied(LocatieDTO regelingsgebied) {
            this.regelingsgebied.add(regelingsgebied);
            regelingsgebied.getRegelingsgebieden().add(this); // Maintain the other side
        }

        public void removeRegelingsgebied(LocatieDTO regelingsgebied) {
            this.regelingsgebied.remove(regelingsgebied);
            regelingsgebied.getRegelingsgebieden().remove(this); // Maintain the other side
        }
    */
    // Helper method to maintain bidirectional relationship if needed, though often not strictly necessary for saving
    public void addLocatie(LocatieDTO locatie) {
        this.regelingsgebied.add(locatie);
        // If LocatieDTO also has a many-to-many back to RegelingDTO (inverse side),
        // you'd add: locatie.getRegelingen().add(this);
        // However, avoid this if RegelingDTO is truly the *owning* side for simplicity in saving.
    }

    public void removeLocatie(LocatieDTO locatie) {
        this.regelingsgebied.remove(locatie);
        // If LocatieDTO has inverse: locatie.getRegelingen().remove(this);
    }

}
