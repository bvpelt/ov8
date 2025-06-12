package com.bsoft.ov8.loader.database;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Embeddable
public class RegistratiegegevensDTO implements Serializable {

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
}
