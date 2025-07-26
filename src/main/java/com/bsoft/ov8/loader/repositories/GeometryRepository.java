package com.bsoft.ov8.loader.repositories;

import com.bsoft.ov8.loader.database.GeometryDTO;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeometryRepository extends PagingAndSortingRepository<GeometryDTO, Long>,
        CrudRepository<GeometryDTO, Long>,
        JpaSpecificationExecutor<GeometryDTO> {

}