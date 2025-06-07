package com.bsoft.ov8.loader.mappers;

import com.bsoft.ov8.loader.database.SoortRegelingDTO;
import nl.overheid.omgevingswet.ozon.model.SoortRegeling;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SoortRegelingMapper {

    @Mapping(source = "code", target = "code")
    @Mapping(source = "waarde", target = "waarde")
    SoortRegelingDTO toSoortRegelingDTO(SoortRegeling soortRegeling);


    // Custom mapping method for 'aangeleverdDoorEen' to a Set<BevoegdGezagDTO>
    @Named("mapSingleSoortRegelingToSet")
    default Set<SoortRegelingDTO> mapSingleSoortRegelingToSet(SoortRegeling soortRegeling) {
        if (soortRegeling == null) {
            return Collections.emptySet(); // Return an empty set if the source is null
        }
        // Map the single SoortRegeling to SoortRegelingDTO
        SoortRegelingDTO dto = toSoortRegelingDTO(soortRegeling);
        // Add it to a new HashSet
        Set<SoortRegelingDTO> soortRegelingDTOSet = new HashSet<>();
        soortRegelingDTOSet.add(dto);
        return soortRegelingDTOSet;
    }
}
