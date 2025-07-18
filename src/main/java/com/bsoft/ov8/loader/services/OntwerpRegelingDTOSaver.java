package com.bsoft.ov8.loader.services;

import com.bsoft.ov8.loader.database.*;
import com.bsoft.ov8.loader.mappers.LocatieMapper;
import com.bsoft.ov8.loader.mappers.OntwerpRegelingMapper;
import com.bsoft.ov8.loader.mappers.RegelingMapper;
import com.bsoft.ov8.loader.repositories.*;
import lombok.extern.slf4j.Slf4j;
import nl.overheid.omgevingswet.ozon.model.EmbeddedLocatie;
import nl.overheid.omgevingswet.ozon.model.Ontwerpregeling;
import nl.overheid.omgevingswet.ozon.model.Regeling;
import nl.overheid.omgevingswet.ozon.model.RegelingAllOfEmbedded;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class OntwerpRegelingDTOSaver {
    private final LocatieMapper locatieMapper;
    private final OntwerpRegelingMapper ontwerpRegelingMapper;

    private final BevoegdGezagRepository bevoegdGezagRepository;
    private final OntwerpRegelingRepository ontwerpRegelingRepository;
    private final SoortRegelingRepository soortRegelingRepository;
    private final LocatieRepository locatieRepository;

    public OntwerpRegelingDTOSaver(BevoegdGezagRepository bevoegdGezagRepository,
                                   OntwerpRegelingRepository ontwerpRegelingRepository,
                                   SoortRegelingRepository soortRegelingRepository,
                                   LocatieRepository locatieRepository,
                                   LocatieMapper locatieMapper,
                                   OntwerpRegelingMapper ontwerpRegelingMapper) {
        this.bevoegdGezagRepository = bevoegdGezagRepository;
        this.ontwerpRegelingRepository = ontwerpRegelingRepository;
        this.soortRegelingRepository = soortRegelingRepository;
        this.locatieRepository = locatieRepository;
        this.locatieMapper = locatieMapper;
        this.ontwerpRegelingMapper = ontwerpRegelingMapper;
    }

    @Transactional
    public OntwerpRegelingDTO saveOntwerpregeling(OntwerpRegelingDTO ontwerpRegelingDTO, Ontwerpregeling ontwerpregeling) {
        log.debug("===> Regeling identificatie {} tijdstipRegistratie: {}, eindRegistratie: {}.",
                ontwerpRegelingDTO.getIdentificatie(),
                ontwerpRegelingDTO.getGeregistreerdMet().getTijdstipRegistratie(),
                ontwerpRegelingDTO.getGeregistreerdMet().getEindRegistratie());

        OntwerpRegelingDTO savedOntwerpRegelingDTO;

        oneToMany(ontwerpRegelingDTO);

        // Check if RegelingDTO already exists
        Optional<OntwerpRegelingDTO> optionalOntwerpRegelingDTO = ontwerpRegelingRepository.findByIdentificatieAndTijdstipregistratieAndEindRegistratie(
                ontwerpRegelingDTO.getIdentificatie(),
                ontwerpRegelingDTO.getGeregistreerdMet().getTijdstipRegistratie(),
                ontwerpRegelingDTO.getGeregistreerdMet().getEindRegistratie());

        if (optionalOntwerpRegelingDTO.isEmpty()) {
            log.debug("+++> New OntwerpRegeling identificatie {} tijdstipRegistratie: {}, eindRegistratie: {} not exists. Saving ontwerpregeling.",
                    ontwerpRegelingDTO.getIdentificatie(),
                    ontwerpRegelingDTO.getGeregistreerdMet().getTijdstipRegistratie(),
                    ontwerpRegelingDTO.getGeregistreerdMet().getEindRegistratie());

            savedOntwerpRegelingDTO = ontwerpRegelingRepository.save(ontwerpRegelingDTO);


            // --- Save the RegelingDTO ---
            // Hibernate will now correctly manage the many-to-many relationship
            // based on the managed entities in regelingDTO.regelingsgebied and the cascade type.
            ontwerpRegelingRepository.save(ontwerpRegelingDTO);
        } else {
            log.debug("---> Existing OntwerpRegeling identificatie {} tijdstipRegistratie: {}, eindRegistratie: {} exists. Skipping save for now. <---",
                    ontwerpRegelingDTO.getIdentificatie(),
                    ontwerpRegelingDTO.getGeregistreerdMet().getTijdstipRegistratie(),
                    ontwerpRegelingDTO.getGeregistreerdMet().getEindRegistratie());

            savedOntwerpRegelingDTO = ontwerpRegelingRepository.save(ontwerpRegelingDTO);
        }

        return savedOntwerpRegelingDTO;
    }

    private void oneToMany(OntwerpRegelingDTO ontwerpRegelingDTO) {
        // --- Handle BevoegdGezagDTO and SoortRegelingDTO (similar logic as before) ---
        if (ontwerpRegelingDTO.getAangeleverdDoorEen() != null) {
            Optional<BevoegdGezagDTO> optionalBevoegdGezagDTO = bevoegdGezagRepository.findByCode(ontwerpRegelingDTO.getAangeleverdDoorEen().getCode());
            BevoegdGezagDTO managedBevoegdGezagDTO;
            if (optionalBevoegdGezagDTO.isEmpty()) {
                managedBevoegdGezagDTO = bevoegdGezagRepository.save(ontwerpRegelingDTO.getAangeleverdDoorEen());
            } else {
                managedBevoegdGezagDTO = optionalBevoegdGezagDTO.get();
            }
            ontwerpRegelingDTO.setAangeleverdDoorEen(managedBevoegdGezagDTO);
        }

        if (ontwerpRegelingDTO.getType() != null) {
            Optional<SoortRegelingDTO> optionalSoortRegelingDTO = soortRegelingRepository.findByCode(ontwerpRegelingDTO.getType() .getCode());
            SoortRegelingDTO managedSoortRegelingDTO;
            if (optionalSoortRegelingDTO.isEmpty()) {
                managedSoortRegelingDTO = soortRegelingRepository.save(ontwerpRegelingDTO.getType() );
            } else {
                managedSoortRegelingDTO = optionalSoortRegelingDTO.get();
            }
            ontwerpRegelingDTO.setType(managedSoortRegelingDTO);
        }
    }
}
