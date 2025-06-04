package com.bsoft.ov8.loader.database;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;

@Data
@Entity
@Table(name = "bevoegdgezag", schema = "public", catalog = "ov8")
public class BevoegdGezagDTO  implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "naam")
    private String naam;

    @Column(name = "bestuurslaag")
    private String bestuurslaag;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "code")
    private String code;

}
