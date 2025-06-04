package com.bsoft.ov8.loader.database;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Entity
@Table(name = "soortregeling", schema = "public", catalog = "ov8")

public class SoortRegelingDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "waarde")
    private String waarde;

}
