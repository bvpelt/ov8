package com.bsoft.ov8.loader.mappers;


import com.bsoft.ov8.loader.database.OntwerpLocatieDTO;
import nl.overheid.omgevingswet.ozon.presenteren.model.EmbeddedOntwerpLocatie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;


@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {UriMapper.class})

@Component
public abstract class OntwerpLocatieMapper {

    @Mapping(source = "identificatie", target = "identificatie", qualifiedByName = "mapUriToString")
    @Mapping(source = "ontwerpbesluitIdentificatie", target = "ontwerpbesluitId", qualifiedByName = "mapUriToString")
    @Mapping(source = "technischId", target = "technischId")
    @Mapping(source = "geometrieIdentificatie", target = "geometrieIdentificatie")
    @Mapping(source = "locatieType", target = "locatieType")
    @Mapping(source = "noemer", target = "noemer")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "boundingBox", target = "boundingBox")
    @Mapping(source = "geregistreerdMet", target = "registratiegegevens")
    public abstract OntwerpLocatieDTO toOntwerpLocatieDTO(EmbeddedOntwerpLocatie locatie);


}