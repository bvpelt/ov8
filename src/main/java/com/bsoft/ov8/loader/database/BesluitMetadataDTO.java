package com.bsoft.ov8.loader.database;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Embeddable
public class BesluitMetadataDTO implements Serializable {

    @Column(name = "besluitciteertitel")
    private String citeerTitel;
}
