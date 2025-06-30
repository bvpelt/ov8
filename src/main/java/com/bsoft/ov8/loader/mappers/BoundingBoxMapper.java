package com.bsoft.ov8.loader.mappers;

import com.bsoft.ov8.loader.database.BoundingBoxDTO;
import com.bsoft.ov8.loader.database.LocatieDTO;
import nl.overheid.omgevingswet.ozon.model.BoundingBox;
import nl.overheid.omgevingswet.ozon.model.EmbeddedLocatie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {UriMapper.class} )

@Component
public abstract class BoundingBoxMapper {

    public abstract BoundingBoxDTO toBoundingBoxDTO(BoundingBox boundingBox);

    public abstract BoundingBox toBoundingBox(BoundingBoxDTO boundingBoxDTO);

}