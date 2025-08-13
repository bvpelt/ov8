package com.bsoft.ov8.loader.repositories;

import com.bsoft.ov8.loader.database.ProcedureStapDTO;
import com.bsoft.ov8.loader.database.ProcedureverloopDTO;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcedureverloopRepository extends PagingAndSortingRepository<ProcedureverloopDTO, Long>,
        CrudRepository<ProcedureverloopDTO, Long>,
        JpaSpecificationExecutor<ProcedureverloopDTO> {

}