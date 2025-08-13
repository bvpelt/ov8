package com.bsoft.ov8.loader.repositories;

import com.bsoft.ov8.loader.database.ProcedureStapDTO;
import com.bsoft.ov8.loader.database.SoortStapDTO;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoortStapRepository extends PagingAndSortingRepository<SoortStapDTO, Long>,
        CrudRepository<SoortStapDTO, Long>,
        JpaSpecificationExecutor<SoortStapDTO> {

}