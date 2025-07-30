package com.bsoft.ov8.loader.repositories;

import com.bsoft.ov8.loader.database.LocatieDTO;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocatieRepository extends PagingAndSortingRepository<LocatieDTO, Long>,
        CrudRepository<LocatieDTO, Long>,
        JpaSpecificationExecutor<LocatieDTO> {

    Optional<LocatieDTO> findByIdentificatieAndGeometrieIdentificatie(String identificatie, String geometrieidentificatie);


    @Query(
            value =
                    "SELECT dg FROM " +
                            "(SELECT DISTINCT(l.geometrieidentificatie) AS dg " +
                            " FROM locatie l LEFT JOIN geo g ON l.geometrieidentificatie = g.geoid " +
                            " WHERE g.geoid IS NULL) " +
                            "WHERE dg IS NOT NULL ORDER BY dg", nativeQuery = true)
    List<String> findNewGeometry();
}