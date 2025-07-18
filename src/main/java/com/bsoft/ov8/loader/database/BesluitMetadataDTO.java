package com.bsoft.ov8.loader.database;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class BesluitMetadataDTO implements Serializable {

    @Column(name = "besluitciteertitel")
    private String citeerTitel;
}
