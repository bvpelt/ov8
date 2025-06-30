package com.bsoft.ov8.loader.mappers;

import com.bsoft.ov8.loader.database.BoundingBoxDTO;
import com.bsoft.ov8.loader.database.RegistratiegegevensDTO;
import nl.overheid.omgevingswet.ozon.model.BoundingBox;
import nl.overheid.omgevingswet.ozon.model.Registratiegegevens;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {UriMapper.class} )

@Component
public abstract class RegistratieGegevensMapper {

    public abstract RegistratiegegevensDTO toRegistratiegegevensDTO(Registratiegegevens registratiegegevens);

    public abstract Registratiegegevens toRegistratiegegevens(RegistratiegegevensDTO registratiegegevensDTO);

}