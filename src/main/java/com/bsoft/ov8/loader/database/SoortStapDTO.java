package com.bsoft.ov8.loader.database;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "code")
@ToString(exclude = "procedurestappen")
@Entity
@Table(name = "soortstap", schema = "public", catalog = "ov8")
public class SoortStapDTO implements Serializable {
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

    @OneToMany(mappedBy = "soortStap", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ProcedureStapDTO> procdurestappen = new HashSet<>(); // Can be named something like 'regelingenUsingThisBevoegdGezag'

}
