package com.bsoft.ov8.loader.database;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nl.overheid.omgevingswet.ozon.model.BoundingBox;
import nl.overheid.omgevingswet.ozon.model.EmbeddedLocatieEmbedded;
import nl.overheid.omgevingswet.ozon.model.LocatieType;
import nl.overheid.omgevingswet.ozon.model.Registratiegegevens;
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
@EqualsAndHashCode(of = "code")
@Entity
@Table(name = "embeddedlocatie", schema = "public", catalog = "ov8")
public class EmbeddedLocatieDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    private URI identificatie;

    private String geometrieIdentificatie;

    private LocatieType locatieType;

    private String noemer;

    //private BoundingBox boundingBox;
    private BigDecimal minX;

    private BigDecimal minY;

    private BigDecimal maxX;

    private BigDecimal maxY;

//    private Registratiegegevens geregistreerdMet;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate beginInwerking;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate beginGeldigheid;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate eindGeldigheid;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime tijdstipRegistratie;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime eindRegistratie;

    //private EmbeddedLocatieEmbedded embedded;
    EmbeddedLocatieDTO omvat;
}
