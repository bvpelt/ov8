package com.bsoft.ov8.loader.database;

import jakarta.persistence.*;
import lombok.*;
import nl.overheid.omgevingswet.ozon.model.*;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"aangeleverdDoorEen", "type" })
@Entity
@Table(name = "regeling", schema = "public", catalog = "ov8")
public class OntwerpRegelingDTO implements Serializable {
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
    private BevoegdGezagDTO aangeleverdDoorEen; // Singular, as each Regeling has one BG

//    private OntwerpregelingLinks links;

    @Column(name = "ontwerpbesluitidentificatie")
    private String ontwerpbesluitIdentificatie;

    @Embedded
    private BesluitMetadataDTO besluitMetadata;

    @Column(name = "technischid")
    private String technischId;

    @Column(name = "expressionid")
    private String expressionId;

//    private SoortRegeling type;
    // This is the "many" side of the Many-to-One relationship.
    // Each RegelingDTO has one BevoegdGezagDTO.
    // The foreign key column will be created in the 'regeling' table.
    @ManyToOne // Default fetch type is EAGER for ManyToOne, consider LAZY if performance is an issue
    @JoinColumn(name = "soortregeling_id") // Specifies the foreign key column name in the RegelingDTO table
    private SoortRegelingDTO type; // Singular, as each Regeling has one BG

    @Embedded
    private OntwerpregistratiegegevensDTO geregistreerdMet;

    @Column(name = "opschrift")
    private String opschrift;

    @Column(name = "citeertitel")
    private String citeerTitel;

    @Column(name = "conditie")
    private String conditie;

    @Column(name = "heeftbijlagen")
    private Boolean heeftBijlagen;

    @Column(name = "heefttoelichtingen")
    private Boolean heeftToelichtingen;

    @Column(name = "isvervangregeling")
    private Boolean isVervangRegeling;

    @Column(name = "publicatieid")
    private String publicatieID;

    @Embedded
    private ProcedureverloopDTO procedureverloop;

    //private OntwerpregelingEmbedded embedded;
}
