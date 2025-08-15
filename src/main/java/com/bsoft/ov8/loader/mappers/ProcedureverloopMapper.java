package com.bsoft.ov8.loader.mappers;

import com.bsoft.ov8.loader.database.ProcedureverloopDTO;
import nl.overheid.omgevingswet.ozon.presenteren.model.Procedureverloop;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = ProcedurestapMapper.class)
public interface ProcedureverloopMapper {

    @Mapping(source = "procedurestappen", target = "procedureStappen", ignore = true)
    ProcedureverloopDTO toDTO(Procedureverloop procedureverloop);

    @Mapping(source = "procedureStappen", target = "procedurestappen", ignore = true)
    Procedureverloop toEntity(ProcedureverloopDTO procedureverloopDTO);

    List<ProcedureverloopDTO> toDTOList(List<Procedureverloop> procedureverloops);

    List<Procedureverloop> toEntityList(List<ProcedureverloopDTO> procedureverloopDTOs);
}