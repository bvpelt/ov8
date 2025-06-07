package com.bsoft.ov8.loader.mappers;

import com.bsoft.ov8.loader.database.BevoegdGezagDTO;
import nl.overheid.omgevingswet.ozon.model.BevoegdGezag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BevoegdGezagMapper {

    @Mapping(source = "naam", target = "naam")
    @Mapping(source = "bestuurslaag", target = "bestuurslaag")
    @Mapping(source = "code", target = "code")
    BevoegdGezagDTO toBevoegdGezagDTO(BevoegdGezag bevoegdGezag);
/*
    // Custom mapping method for 'aangeleverdDoorEen' to a Set<BevoegdGezagDTO>
    @Named("mapSingleBevoegdGezagToSet")
    default Set<BevoegdGezagDTO> mapSingleBevoegdGezagToSet(BevoegdGezag bevoegdGezag) {
        if (bevoegdGezag == null) {
            return Collections.emptySet(); // Return an empty set if the source is null
        }
        // Map the single BevoegdGezag to BevoegdGezagDTO
        BevoegdGezagDTO dto = toBevoegdGezagDTO(bevoegdGezag);
        // Add it to a new HashSet
        Set<BevoegdGezagDTO> bevoegdGezagDTOSet = new HashSet<>();
        bevoegdGezagDTOSet.add(dto);
        return bevoegdGezagDTOSet;
    }

 */
}
