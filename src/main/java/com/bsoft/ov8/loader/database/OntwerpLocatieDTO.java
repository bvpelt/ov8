package com.bsoft.ov8.loader.database;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nl.overheid.omgevingswet.ozon.presenteren.model.LocatieType;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = {"identificatie", "ontwerpbesluitid", "technischid", "geometrieIdentificatie"})
@ToString(exclude = {"regelingsgebieden"})
@Entity
@Table(name = "ontwerplocatie", schema = "public", catalog = "ov8")
public class OntwerpLocatieDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "identificatie")
    private String identificatie;

    @Column(name = "ontwerpbesluitid")
    private String ontwerpbesluitId;

    @Column(name = "technischid")
    private String technischId;

    @Column(name = "geometrieidentificatie")
    private String geometrieIdentificatie;

    @Enumerated(EnumType.STRING)
    @Column(name = "locatietype")
    private LocatieType locatieType;

    @Column(name = "noemer")
    private String noemer;

    @Column(name = "status")
    private String status;

    @Embedded
    private BoundingBoxDTO boundingBox;

    @Embedded
    private RegistratiegegevensDTO registratiegegevens;

    // This is the inverse side of the Many-to-Many relationship
    @ManyToMany(mappedBy = "regelingsgebied") //, fetch = FetchType.LAZY)
    private Set<OntwerpRegelingDTO> regelingsgebieden = new HashSet<>(); // Naming convention `regelingsgebieds` for the collection

    // This field represents the "one" side (the group).
    // It is the parent of a hierarchical relationship.
    // 'mappedBy' points to the field on the *child* (member) side that refers back to this parent.
    @OneToMany(
            mappedBy = "parentGroup", // The field 'parentGroup' in the *member* LocatieDTO
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}, // Operations on the group cascade to its members
            orphanRemoval = false // Be cautious with orphanRemoval: true means deleting a group deletes its members
    )
    private Set<OntwerpLocatieDTO> members = new HashSet<>(); // Renamed 'groep' to 'members' for clarity

    // This field represents the "many" side (the member).
    // It holds a reference to the parent Group LocatieDTO if this LocatieDTO is a member of a group.
    // This is the owning side for the foreign key.
    @ManyToOne(fetch = FetchType.LAZY) // Lazy loading is good practice for many-to-one
    @JoinColumn(name = "parent_group_id") // This will create the foreign key column in the 'locatie' table
    private OntwerpLocatieDTO parentGroup; // Represents the group this location belongs to

    // For self-referencing group relationship
    // Call this on a LocatieDTO instance that IS a GROUP
    public void addMember(OntwerpLocatieDTO member) {
        if (member != null) {
            getMembers().add(member);
            member.setParentGroup(this); // Set the back-reference on the member
        }
    }

    // Call this on a LocatieDTO instance that IS a GROUP
    public void removeMember(OntwerpLocatieDTO member) {
        if (member != null) {
            getMembers().remove(member);
            member.setParentGroup(null); // Clear the back-reference on the member
        }
    }
}
