package com.bsoft.ov8.loader.repositories;

import com.bsoft.ov8.loader.database.RegelingDTO;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface RegelingRepository extends PagingAndSortingRepository<RegelingDTO, Long>,
        CrudRepository<RegelingDTO, Long>,
        JpaSpecificationExecutor<RegelingDTO> {

    Optional<RegelingDTO> findByIdentificatieAndTijdstipRegistratieAndBeginGeldigheid (String identificatie, OffsetDateTime tijdstipRegistratie, LocalDate beginGeldigheid);
}