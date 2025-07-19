package com.bsoft.ov8.loader.database;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "code")
//@ToString(exclude = "regelingen")
@Entity
@Table(name = "procedurestap", schema = "public", catalog = "ov8")
public class ProcedureStapDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne // Default fetch type is EAGER for ManyToOne, consider LAZY if performance is an issue
    @JoinColumn(name = "soortstap_id")
    private SoortStapDTO soortStap;

    @Column(name = "actor")
    private String actor;

    @Column(name = "voltooidop")
    private String voltooidOp;
}
