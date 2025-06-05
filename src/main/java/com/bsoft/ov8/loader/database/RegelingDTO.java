package com.bsoft.ov8.loader.database;

import com.bsoft.ov8.generated.model.*;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
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
    private URI identificatie;

    @Column(name = "officieleTitel")
    private String officieleTitel;

    //
    // aangeleverd door bevoegd gezag
    //
    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    }, fetch = FetchType.LAZY)
    @JoinTable(name = "regeling_bevoegdgezag", // Name of the join table
            joinColumns = @JoinColumn(name = "regeling_id"), // Foreign key for Regeling
            inverseJoinColumns = @JoinColumn(name = "bevoegdgezag_id")) // Foreign key for BevoegdGezag
    private Set<BevoegdGezagDTO> bevoegdGezagen = new HashSet<>();

    /*
    @Column(name = "links")
    private RegelingLinks links;
    */

    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    }, fetch = FetchType.LAZY)
    @JoinTable(name = "regeling_soortregeling", // Name of the join table
            joinColumns = @JoinColumn(name = "regeling_id"), // Foreign key for Regeling
            inverseJoinColumns = @JoinColumn(name = "soortregeling_id")) // Foreign key for Soortregeling
    private Set<SoortRegelingDTO> type = new HashSet<>();

    //
    // registratie gegevens
    //
    @Column(name = "versie")
    private BigDecimal versie;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "beginInwerking")
    private LocalDate beginInwerking;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "beginGeldigheid")
    private LocalDate beginGeldigheid;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "eindGeldigheid")
    private LocalDate eindGeldigheid;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "tijdstipRegistratie")
    private OffsetDateTime tijdstipRegistratie;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "eindRegistratie")
    private OffsetDateTime eindRegistratie;

    //
    // einde registratie gegevens
    //

    @Column(name = "citeerTitel")
    private String citeerTitel;

    @Column(name = "opschrift")
    private String opschrift;

    @Column(name = "conditie")
    private String conditie;

    /*
    @Column(name = "opvolgerVan")
    private List<RegelingDTO> opvolgerVan = new ArrayList();
    */

    @Column(name = "heeftBijlagen")
    private Boolean heeftBijlagen;

    @Column(name = "heeftToelichtingen")
    private Boolean heeftToelichtingen;

    @Column(name = "publicatieID")
    private String publicatieID;

    @Column(name = "inwerkingTot")
    private String inwerkingTot;

    @Column(name = "geldigTot")
    private String geldigTot;

    /*
    @Column(name = "embedded")
    private RegelingAllOfEmbedded embedded;
     */

    // Helper methods to manage the relationship (optional but good practice)
    public void addBevoegdGezag(BevoegdGezagDTO bevoegdGezag) {
        this.bevoegdGezagen.add(bevoegdGezag);
        bevoegdGezag.getRegelingen().add(this); // Maintain the other side
    }

    public void removeBevoegdGezag(BevoegdGezagDTO bevoegdGezag) {
        this.bevoegdGezagen.remove(bevoegdGezag);
        bevoegdGezag.getRegelingen().remove(this); // Maintain the other side
    }

    public void addSoortRegeling(SoortRegelingDTO soortRegeling) {
        this.type.add(soortRegeling);
        soortRegeling.getRegelingen().add(this); // Maintain the other side
    }

    public void removeSoortRegeling(SoortRegelingDTO soortRegeling) {
        this.type.remove(soortRegeling);
        soortRegeling.getRegelingen().remove(this); // Maintain the other side
    }
}
