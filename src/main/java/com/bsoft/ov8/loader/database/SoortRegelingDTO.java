package com.bsoft.ov8.loader.database;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = "code")
@ToString(exclude = "regelingen")
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
/*
    @ManyToMany(mappedBy = "type", fetch = FetchType.LAZY) // 'mappedBy' indicates the owning side
    private Set<RegelingDTO> regelingen = new HashSet<>();
*/
    // This is the "one" side of the Many-to-One relationship from RegelingDTO.
    // It's the inverse side, used for navigation from BevoegdGezag to all Regelingen using it.
    // 'mappedBy' refers to the field name in the 'RegelingDTO' entity that owns the relationship.
    @OneToMany(mappedBy = "type", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<RegelingDTO> regelingen = new HashSet<>(); // Can be named something like 'regelingenUsingThisBevoegdGezag'

}
