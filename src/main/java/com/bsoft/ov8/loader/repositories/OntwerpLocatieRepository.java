package com.bsoft.ov8.loader.repositories;

import com.bsoft.ov8.loader.database.LocatieDTO;
import com.bsoft.ov8.loader.database.OntwerpLocatieDTO;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OntwerpLocatieRepository extends PagingAndSortingRepository<OntwerpLocatieDTO, Long>,
        CrudRepository<OntwerpLocatieDTO, Long>,
        JpaSpecificationExecutor<OntwerpLocatieDTO> {

    Optional<OntwerpLocatieDTO> findByIdentificatieAndGeometrieIdentificatie(String identificatie, String geometrieidentificatie);

}