package com.bsoft.ov8.loader.database;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Embeddable
public class BoundingBoxDTO implements Serializable {

    @Column(name = "minx")
    private BigDecimal minX;

    @Column(name = "miny")
    private BigDecimal minY;

    @Column(name = "maxx")
    private BigDecimal maxX;

    @Column(name = "maxy")
    private BigDecimal maxY;
}
