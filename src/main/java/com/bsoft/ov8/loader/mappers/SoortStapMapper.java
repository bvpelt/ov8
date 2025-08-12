package com.bsoft.ov8.loader.mappers;

import com.bsoft.ov8.loader.database.SoortStapDTO;
import nl.overheid.omgevingswet.ozon.presenteren.model.SoortStap;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SoortStapMapper {

    SoortStapDTO toDTO(SoortStap soortStap);

    SoortStap toEntity(SoortStapDTO soortStapDTO);

    List<SoortStapDTO> toDTOList(List<SoortStap> soortStappen);

    List<SoortStap> toEntityList(List<SoortStapDTO> soortStapDTOs);
}