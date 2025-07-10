package com.bsoft.ov8.loader.repositories;

import com.bsoft.ov8.loader.database.RegelingDTO;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegelingRepository extends PagingAndSortingRepository<RegelingDTO, Long>,
        CrudRepository<RegelingDTO, Long>,
        JpaSpecificationExecutor<RegelingDTO> {

    @Query(
            value =
                    "SELECT * FROM regeling WHERE identificatie = :identificatie and tijdstipregistratie=:tijdstipRegistratie and begingeldigheid = :beginGeldigheid", nativeQuery = true)
    Optional<RegelingDTO> findByIdentificatieAndTijdstipregistratieAndBegingeldigheid(String identificatie, OffsetDateTime tijdstipRegistratie, LocalDate beginGeldigheid);

    @Query(
            value =
                    "SELECT * FROM regeling WHERE identificatie = :identificatie and tijdstipregistratie=:tijdstipRegistratie and begingeldigheid = :beginGeldigheid", nativeQuery = true)
    Optional<RegelingDTO> existsByIdentificatieAndRegistratiegegevens_BeginGeldigheidAndRegistratiegegevens_BeginInwerking(String identificatie, OffsetDateTime tijdstipRegistratie, LocalDate beginGeldigheid);


    @Query(
            value =
                    "SELECT * FROM regeling WHERE versie > :version order by identificatie", nativeQuery = true)
    List<RegelingDTO> findByVersieGreaterThan(Integer version);
}