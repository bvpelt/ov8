package com.bsoft.ov8.loader.repositories;

import com.bsoft.ov8.loader.database.OntwerpRegelingDTO;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface OntwerpRegelingRepository extends PagingAndSortingRepository<OntwerpRegelingDTO, Long>,
        CrudRepository<OntwerpRegelingDTO, Long>,
        JpaSpecificationExecutor<OntwerpRegelingDTO> {

    @Query(
            value =
                    "SELECT * FROM ontwerpregeling WHERE identificatie = :identificatie and tijdstipregistratie=:tijdstipRegistratie and eindregistratie = :eindRegistratie", nativeQuery = true)
    Optional<OntwerpRegelingDTO> findByIdentificatieAndTijdstipregistratieAndEindRegistratie(String identificatie, OffsetDateTime tijdstipRegistratie, OffsetDateTime eindRegistratie);
}