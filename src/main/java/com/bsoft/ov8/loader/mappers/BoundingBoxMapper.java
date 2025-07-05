package com.bsoft.ov8.loader.mappers;

import com.bsoft.ov8.loader.database.BoundingBoxDTO;
import nl.overheid.omgevingswet.ozon.model.BoundingBox;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {UriMapper.class})

@Component
public abstract class BoundingBoxMapper {

    public abstract BoundingBoxDTO toBoundingBoxDTO(BoundingBox boundingBox);

    public abstract BoundingBox toBoundingBox(BoundingBoxDTO boundingBoxDTO);

}