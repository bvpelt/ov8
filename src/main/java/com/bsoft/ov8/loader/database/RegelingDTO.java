package com.bsoft.ov8.loader.database;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"bevoegdGezag", "type"})
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
/*
    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    }, fetch = FetchType.LAZY)
    @JoinTable(name = "regeling_soortregeling", // Name of the join table
            joinColumns = @JoinColumn(name = "regeling_id"), // Foreign key for Regeling
            inverseJoinColumns = @JoinColumn(name = "soortregeling_id")) // Foreign key for Soortregeling
    private Set<SoortRegelingDTO> type = new HashSet<>();
*/
    // This is the "many" side of the Many-to-One relationship.
    // Each RegelingDTO has one BevoegdGezagDTO.
    // The foreign key column will be created in the 'regeling' table.
    @ManyToOne // Default fetch type is EAGER for ManyToOne, consider LAZY if performance is an issue
    @JoinColumn(name = "soortregeling_id") // Specifies the foreign key column name in the RegelingDTO table
    private SoortRegelingDTO type; // Singular, as each Regeling has one BG

    //
    // registratie gegevens
    //
    @Column(name = "versie")
    private BigDecimal versie;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "begininwerking")
    private LocalDate beginInwerking;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "begingeldigheid")
    private LocalDate beginGeldigheid;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "eindgeldigheid")
    private LocalDate eindGeldigheid;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "tijdstipregistratie")
    private OffsetDateTime tijdstipRegistratie;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "eindregistratie")
    private OffsetDateTime eindRegistratie;

    //
    // einde registratie gegevens
    //

    @Column(name = "citeertitel")
    private String citeerTitel;

    @Column(name = "opschrift")
    private String opschrift;

    @Column(name = "conditie")
    private String conditie;

    /*
    @Column(name = "opvolgerVan")
    private List<RegelingDTO> opvolgerVan = new ArrayList();
    */

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

}
