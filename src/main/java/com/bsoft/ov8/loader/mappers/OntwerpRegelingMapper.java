package com.bsoft.ov8.loader.mappers;

import com.bsoft.ov8.loader.database.OntwerpRegelingDTO;
import com.bsoft.ov8.loader.database.RegelingDTO;
import com.bsoft.ov8.loader.repositories.RegelingRepository;
import nl.overheid.omgevingswet.ozon.model.Ontwerpregeling;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {BevoegdGezagMapper.class, SoortRegelingMapper.class, UriMapper.class}
)

@Component
public abstract class OntwerpRegelingMapper {

    // This method defines the mapping from Regeling to RegelingDTO
    @Mapping(source = "identificatie", target = "identificatie", qualifiedByName = "mapUriToString")
    @Mapping(source = "officieleTitel", target = "officieleTitel")
    @Mapping(source = "aangeleverdDoorEen", target = "aangeleverdDoorEen")
    @Mapping(source = "ontwerpbesluitIdentificatie", target = "ontwerpbesluitIdentificatie", qualifiedByName = "mapUriToString")
    @Mapping(source = "besluitMetadata", target="besluitMetadata")
    @Mapping(source = "technischId", target = "technischId")
    @Mapping(source = "expressionId", target="expressionId", qualifiedByName = "mapUriToString")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "geregistreerdMet", target = "geregistreerdMet")
    @Mapping(source = "opschrift", target = "opschrift")
    @Mapping(source = "citeerTitel", target = "citeerTitel")
    @Mapping(source = "conditie", target = "conditie")
    @Mapping(source = "heeftBijlagen", target = "heeftBijlagen")
    @Mapping(source = "heeftToelichtingen", target = "heeftToelichtingen")
    @Mapping(source = "isVervangRegeling", target="isVervangRegeling")
    @Mapping(source = "publicatieID", target = "publicatieID")
    @Mapping(source = "procedureverloop", target="procedureverloop")
    public abstract OntwerpRegelingDTO toOntwerpRegelingDTO(Ontwerpregeling ontwerpRegeling);


}