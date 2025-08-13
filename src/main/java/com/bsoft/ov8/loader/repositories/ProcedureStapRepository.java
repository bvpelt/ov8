package com.bsoft.ov8.loader.repositories;

import com.bsoft.ov8.loader.database.ProcedureStapDTO;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcedureStapRepository extends PagingAndSortingRepository<ProcedureStapDTO, Long>,
        CrudRepository<ProcedureStapDTO, Long>,
        JpaSpecificationExecutor<ProcedureStapDTO> {

}