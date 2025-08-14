package com.bsoft.ov8.loader.mappers;

import com.bsoft.ov8.loader.database.ProcedureStapDTO;
import com.bsoft.ov8.loader.database.ProcedureverloopDTO;
import nl.overheid.omgevingswet.ozon.presenteren.model.Procedurestap;
import nl.overheid.omgevingswet.ozon.presenteren.model.Procedureverloop;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring", uses = SoortStapMapper.class)
public interface ProcedurestapMapper {

    @Mapping(source="soortStap", target="soortStap")
    ProcedureStapDTO toDTO(Procedurestap procedurestap);

    @Mapping(source="soortStap", target="soortStap")
    Procedurestap toEntity(ProcedureStapDTO procedureStapDTO);

    List<ProcedureStapDTO> toDTOList(List<Procedurestap> procedurestappen);

    List<Procedurestap> toEntityList(List<ProcedureStapDTO> procedureStapDTOs);
}