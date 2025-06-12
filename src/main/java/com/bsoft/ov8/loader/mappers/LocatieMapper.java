package com.bsoft.ov8.loader.mappers;

import com.bsoft.ov8.loader.database.LocatieDTO;
import nl.overheid.omgevingswet.ozon.model.EmbeddedLocatie;
// Removed: import nl.overheid.omgevingswet.ozon.model.Locatie; // If not used
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

// Removed: import java.net.URI; // If not used directly here
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {UriMapper.class} )

@Component
public abstract class LocatieMapper {

    @Mapping(source = "identificatie", target = "identificatie", qualifiedByName = "mapUriToString")
    @Mapping(source = "geometrieIdentificatie", target = "geometrieIdentificatie")
    @Mapping(source = "locatieType", target = "locatieType")
    @Mapping(source = "noemer", target = "noemer")
    @Mapping(source = "boundingBox", target = "boundingBox")
    @Mapping(source = "geregistreerdMet", target = "registratiegegevens")
    public abstract LocatieDTO toLocatieDTO(EmbeddedLocatie locatie);

    /**
     * Maps a list of EmbeddedLocatie to a Set of LocatieDTO.
     * MapStruct will automatically use toLocatieDTO(EmbeddedLocatie) for each element.
     * Keep this if 'regelingsgebied' can sometimes be a List in the source API model.
     */
    public abstract Set<LocatieDTO> toLocatieDTOSet(List<EmbeddedLocatie> embeddedLocaties);

    /**
     * Custom mapping method to convert a single EmbeddedLocatie into a Set<LocatieDTO>.
     * This method is concrete (not abstract) and provides its own implementation.
     * We give it a @Named qualifier so RegelingMapper can specifically call it.
     */
    @Named("mapSingleEmbeddedLocatieToSet") // New name for this specific mapping
    public Set<LocatieDTO> mapSingleEmbeddedLocatieToSet(EmbeddedLocatie embeddedLocatie) {
        if (embeddedLocatie == null) {
            return new HashSet<>(); // Return an empty set for null input
        }
        HashSet<LocatieDTO> locatieDTOs = new HashSet<>();
        locatieDTOs.add(toLocatieDTO(embeddedLocatie)); // Map the single EmbeddedLocatie
        return locatieDTOs;
    }
}