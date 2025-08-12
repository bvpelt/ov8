package com.bsoft.ov8.loader.database;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(exclude = {"ontwerpRegeling", "procedureStappen"}) // Prevent circular references
@ToString(exclude = {"ontwerpRegeling", "procedureStappen"}) // Prevent circular references
@Entity
@Table(name = "procedureverloop", schema = "public", catalog = "ov8")
public class ProcedureverloopDTO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "bekendop")
    private String bekendOp;

    @Column(name = "ontvangenop")
    private String ontvangenOp;

    // 1:1 relationship back to OntwerpRegelingDTO (owning side)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ontwerpregeling_id", referencedColumnName = "id")
    private OntwerpRegelingDTO ontwerpRegeling;

    @OneToMany(mappedBy = "procedureverloop", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ProcedureStapDTO> procedureStappen = new ArrayList<>();

    // Convenience methods for managing bidirectional relationships
    public void addProcedureStap(ProcedureStapDTO procedureStap) {
        procedureStappen.add(procedureStap);
        procedureStap.setProcedureverloop(this);
    }

    public void removeProcedureStap(ProcedureStapDTO procedureStap) {
        procedureStappen.remove(procedureStap);
        procedureStap.setProcedureverloop(null);
    }

    public void clearProcedureStappen() {
        procedureStappen.forEach(stap -> stap.setProcedureverloop(null));
        procedureStappen.clear();
    }
}
