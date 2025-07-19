package com.bsoft.ov8.loader.database;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class ProcedureverloopDTO implements Serializable {

    @Column(name = "bekendop")
    private String bekendOp;

    @Column(name = "ontvangenop")
    private String ontvangenOp;
}
