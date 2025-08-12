package com.bsoft.ov8.loader.mappers;

import com.bsoft.ov8.loader.database.OntwerpRegelingDTO;
import com.bsoft.ov8.loader.database.ProcedureverloopDTO;
import nl.overheid.omgevingswet.ozon.presenteren.model.Ontwerpregeling;
import nl.overheid.omgevingswet.ozon.presenteren.model.Procedureverloop;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring", uses = ProcedurestapMapper.class)
public interface ProcedureverloopMapper {

    ProcedureverloopDTO toDTO(Procedureverloop procedureverloop);

    Procedureverloop toEntity(ProcedureverloopDTO procedureverloopDTO);

    List<ProcedureverloopDTO> toDTOList(List<Procedureverloop> procedureverloops);

    List<Procedureverloop> toEntityList(List<ProcedureverloopDTO> procedureverloopDTOs);
}