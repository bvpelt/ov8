package com.bsoft.ov8.loader.database;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "registratiegegevens", schema = "public", catalog = "ov8")
public class RegistratiegegevensDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private BigDecimal id;

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
}
