package com.bsoft.ov8.loader.database;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Geometry;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "geoid")
@Entity
@Table(name = "geo", schema = "public", catalog = "ov8")
public class GeometryDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "geoid")
    private String geoid;

    @Column(name = "geometrie")
    private Geometry geometrie;

}
