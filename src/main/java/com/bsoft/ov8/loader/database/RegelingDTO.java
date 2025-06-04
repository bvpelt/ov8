package com.bsoft.ov8.loader.database;

import com.bsoft.ov8.generated.model.*;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "regeling", schema = "public", catalog = "ov8")
public class RegelingDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "identificatie")
    private URI identificatie;

    @Column(name = "officieleTitel")
    private String officieleTitel;


    @ManyToOne
    @JoinColumn(name = "code")
    private BevoegdGezagDTO aangeleverdDoorEen;

    /*
    @Column(name = "links")
    private RegelingLinks links;
*/

    @ManyToOne
    @JoinColumn(name = "code")
    private SoortRegelingDTO type;

    @Column(name = "geregistreerdMet")
    private Registratiegegevens geregistreerdMet;

    @Column(name = "citeerTitel")
    private String citeerTitel;

    @Column(name = "opschrift")
    private String opschrift;

    @Column(name = "conditie")
    private String conditie;

    @Column(name = "opvolgerVan")
    private List<Regeling> opvolgerVan = new ArrayList();

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

    @Column(name = "embedded")
    private RegelingAllOfEmbedded embedded;
}
