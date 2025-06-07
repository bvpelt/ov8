package com.bsoft.ov8.loader.mappers;

import com.bsoft.ov8.loader.database.RegelingDTO;
import nl.overheid.omgevingswet.ozon.model.Regeling;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.net.URI;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {BevoegdGezagMapper.class, SoortRegelingMapper.class} // IMPORTANT: Tell MapStruct to use BevoegdGezagMapper
)

public interface RegelingMapper {

    // This method defines the mapping from Regeling to RegelingDTO
    @Mapping(source = "identificatie", target = "identificatie", qualifiedByName = "mapUriToString")
    @Mapping(target = "officieleTitel", source = "officieleTitel")
    @Mapping(source = "aangeleverdDoorEen", target = "bevoegdGezag")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "geregistreerdMet.versie", target = "versie")
    @Mapping(source = "geregistreerdMet.beginInwerking", target = "beginInwerking")
    @Mapping(source = "geregistreerdMet.beginGeldigheid", target = "beginGeldigheid")
    @Mapping(source = "geregistreerdMet.eindGeldigheid", target = "eindGeldigheid")
    @Mapping(source = "geregistreerdMet.tijdstipRegistratie", target = "tijdstipRegistratie")
    @Mapping(source = "geregistreerdMet.eindRegistratie", target = "eindRegistratie")
    @Mapping(target = "citeerTitel", source = "citeerTitel")
    @Mapping(target = "opschrift", source = "opschrift")
    @Mapping(target = "conditie", source = "conditie")
    @Mapping(target = "heeftBijlagen", source = "heeftBijlagen")
    @Mapping(target = "heeftToelichtingen", source = "heeftToelichtingen")
    @Mapping(target = "publicatieID", source = "publicatieID")
    @Mapping(target = "inwerkingTot", source = "inwerkingTot")
    @Mapping(target = "geldigTot", source = "geldigTot")
    RegelingDTO toRegelingDTO(Regeling regeling);

    // Custom mapping method for 'aangeleverdDoorEen' to a Set<BevoegdGezagDTO>
    @Named("mapUriToString")
    default String mapUriToString(java.net.URI uri) {
        return uri != null ? uri.toString() : null;
    }

}
