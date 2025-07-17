package com.bsoft.ov8.loader.database;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class ProcedureverloopDTO implements Serializable {

    @Column(name = "bekendOp")
    private String bekendOp;

    @Column(name = "ontvangenOp")
    private String ontvangenOp;
}
