package com.bsoft.ov8.loader.mappers;

import com.bsoft.ov8.loader.database.BevoegdGezagDTO;
import nl.overheid.omgevingswet.ozon.presenteren.model.BevoegdGezag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BevoegdGezagMapper {

    @Mapping(source = "naam", target = "naam")
    @Mapping(source = "bestuurslaag", target = "bestuurslaag")
    @Mapping(source = "code", target = "code")
    BevoegdGezagDTO toBevoegdGezagDTO(BevoegdGezag bevoegdGezag);

}
