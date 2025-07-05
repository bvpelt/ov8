package com.bsoft.ov8.loader.repositories;

import com.bsoft.ov8.loader.database.LocatieDTO;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocatieRepository extends PagingAndSortingRepository<LocatieDTO, Long>,
        CrudRepository<LocatieDTO, Long>,
        JpaSpecificationExecutor<LocatieDTO> {

    Optional<LocatieDTO> findByIdentificatieAndGeometrieIdentificatie(String identificatie, String geometrieidentificatie);
}