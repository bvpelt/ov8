package com.bsoft.ov8.loader.repositories;

import com.bsoft.ov8.loader.database.BevoegdGezagDTO;
import com.bsoft.ov8.loader.database.SoortRegelingDTO;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SoortRegelingRepository extends PagingAndSortingRepository<SoortRegelingDTO, Long>,
        CrudRepository<SoortRegelingDTO, Long>,
        JpaSpecificationExecutor<SoortRegelingDTO> {

    Optional<SoortRegelingDTO> findByCode(String code);
}