package com.bsoft.ov8.loader.database;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "soortregeling", schema = "public", catalog = "ov8")

public class SoortRegelingDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "waarde")
    private String waarde;

    @ManyToMany(mappedBy = "type", fetch = FetchType.LAZY) // 'mappedBy' indicates the owning side
    private Set<RegelingDTO> regelingen = new HashSet<>();
}
