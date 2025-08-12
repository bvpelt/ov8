package com.bsoft.ov8.loader.utils;

import com.bsoft.ov8.loader.database.*;
import com.bsoft.ov8.loader.mappers.LocatieMapper;
import com.bsoft.ov8.loader.mappers.OntwerpLocatieMapper;
import com.bsoft.ov8.loader.mappers.OntwerpRegelingMapper;
import com.bsoft.ov8.loader.repositories.*;
import lombok.extern.slf4j.Slf4j;
import nl.overheid.omgevingswet.ozon.presenteren.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class OntwerpRegelingDTOSaver {
    private final LocatieMapper locatieMapper;
    private final OntwerpLocatieMapper ontwerpLocatieMapper;
    private final OntwerpRegelingMapper ontwerpRegelingMapper;

    private final BevoegdGezagRepository bevoegdGezagRepository;
    private final OntwerpRegelingRepository ontwerpRegelingRepository;
    private final SoortRegelingRepository soortRegelingRepository;
    private final LocatieRepository locatieRepository;
    private final OntwerpLocatieRepository ontwerpLocatieRepository;

    public OntwerpRegelingDTOSaver(BevoegdGezagRepository bevoegdGezagRepository,
                                   OntwerpRegelingRepository ontwerpRegelingRepository,
                                   SoortRegelingRepository soortRegelingRepository,
                                   LocatieRepository locatieRepository,
                                   OntwerpLocatieRepository ontwerpLocatieRepository,
                                   LocatieMapper locatieMapper,
                                   OntwerpLocatieMapper ontwerpLocatieMapper,
                                   OntwerpRegelingMapper ontwerpRegelingMapper) {
        this.bevoegdGezagRepository = bevoegdGezagRepository;
        this.ontwerpRegelingRepository = ontwerpRegelingRepository;
        this.soortRegelingRepository = soortRegelingRepository;
        this.locatieRepository = locatieRepository;
        this.ontwerpLocatieRepository = ontwerpLocatieRepository;
        this.locatieMapper = locatieMapper;
        this.ontwerpLocatieMapper = ontwerpLocatieMapper;
        this.ontwerpRegelingMapper = ontwerpRegelingMapper;
    }

    @Transactional
    public OntwerpRegelingDTO saveOntwerpregeling(OntwerpRegelingDTO ontwerpRegelingDTO, Ontwerpregeling ontwerpregeling) {
        log.debug("===> Regeling identificatie {} tijdstipRegistratie: {}, eindRegistratie: {}.",
                ontwerpRegelingDTO.getIdentificatie(),
                ontwerpRegelingDTO.getGeregistreerdMet().getTijdstipRegistratie(),
                ontwerpRegelingDTO.getGeregistreerdMet().getEindRegistratie());

        OntwerpRegelingDTO managedOntwerpRegelingDTO;

        // Check if RegelingDTO already exists
        Optional<OntwerpRegelingDTO> optionalOntwerpRegelingDTO = ontwerpRegelingRepository.findByIdentificatieAndTijdstipregistratieAndEindRegistratie(
                ontwerpRegelingDTO.getIdentificatie(),
                ontwerpRegelingDTO.getGeregistreerdMet().getTijdstipRegistratie(),
                ontwerpRegelingDTO.getGeregistreerdMet().getEindRegistratie());

        // Procedurestappen

        // Locaties
        if (ontwerpregeling.getEmbedded() != null) {
            log.debug("01 ontwerpregeling has embedded");
            EmbeddedOntwerpLocatie embeddedOntwerpLocatie = ontwerpregeling.getEmbedded().getOntwerpRegelingsgebied();
            if (embeddedOntwerpLocatie != null) {
                log.debug("02 embeddedOntwerpLocatie has embedded ontwerplocatie");
                OntwerpLocatieDTO ontwerpLocatie = ontwerpLocatieMapper.toOntwerpLocatieDTO(embeddedOntwerpLocatie);

                OntwerpLocatieDTO managedOntwerpLocatieDTO;
                Optional<OntwerpLocatieDTO> optionalOntwerpLocatieDTO = ontwerpLocatieRepository.findByIdentificatieAndGeometrieIdentificatie(ontwerpLocatie.getIdentificatie(), ontwerpLocatie.getGeometrieIdentificatie());
                if (optionalOntwerpLocatieDTO.isEmpty()) {
                    log.debug("03 ontwerplocatie not present saving: {}, {}", ontwerpLocatie.getIdentificatie(), ontwerpLocatie.getGeometrieIdentificatie());
                    managedOntwerpLocatieDTO = ontwerpLocatieRepository.save(ontwerpLocatie);
                }   else {
                    log.debug("04 ontwerplocatie present using: {}, {}", optionalOntwerpLocatieDTO.get().getIdentificatie(), optionalOntwerpLocatieDTO.get().getGeometrieIdentificatie());
                    managedOntwerpLocatieDTO = optionalOntwerpLocatieDTO.get();
                }

                EmbeddedOntwerpLocatieEmbedded embeddedOntwerpLocatieEmbedded = embeddedOntwerpLocatie.getEmbedded();
                if (embeddedOntwerpLocatieEmbedded != null) {
                    List<EmbeddedOntwerpLocatie> embeddedOntwerpLocatieList = embeddedOntwerpLocatieEmbedded.getOmvat();
                    List<EmbeddedLocatie> embeddedLocatieList = embeddedOntwerpLocatieEmbedded.getOmvatVastgesteld();

                    log.debug("05 # omvat: {}, # omvatvastgesteld {}", embeddedOntwerpLocatieList.size(), embeddedLocatieList.size());
                    List<OntwerpLocatieDTO> omvat = new ArrayList<>();
                    embeddedOntwerpLocatieList.forEach(embeddedLocatie -> {
                        OntwerpLocatieDTO locatie = ontwerpLocatieMapper.toOntwerpLocatieDTO(embeddedLocatie);
                        log.debug("06 check locatie: {}, {}", locatie.getIdentificatie(), locatie.getGeometrieIdentificatie());
                        Optional<OntwerpLocatieDTO> optionalLocatieDTO = ontwerpLocatieRepository.findByIdentificatieAndGeometrieIdentificatie(locatie.getIdentificatie(), locatie.getGeometrieIdentificatie());
                        if (!optionalLocatieDTO.isPresent()) {
                            log.debug("07 locatie not present saving: {}, {}", locatie.getIdentificatie(), locatie.getGeometrieIdentificatie());
                            locatie.setParentGroup(managedOntwerpLocatieDTO);
                            OntwerpLocatieDTO managedLocatieDTO = ontwerpLocatieRepository.save(locatie);
                            omvat.add(managedLocatieDTO);
                        }
                    });

                    List<LocatieDTO> omvatVastgesteld = new ArrayList<>();
                    embeddedLocatieList.forEach(embeddedLocatie -> {
                        LocatieDTO locatie = locatieMapper.toLocatieDTO(embeddedLocatie);
                        log.debug("08 check locatie: {}, {}", locatie.getIdentificatie(), locatie.getGeometrieIdentificatie());
                        Optional<LocatieDTO> optionalLocatieDTO = locatieRepository.findByIdentificatieAndGeometrieIdentificatie(locatie.getIdentificatie(), locatie.getGeometrieIdentificatie());
                        if (!optionalLocatieDTO.isPresent()) {
                            log.debug("09 locatie not present saving: {}, {}", locatie.getIdentificatie(), locatie.getGeometrieIdentificatie());
                           // locatie.setParentGroup(managedOntwerpLocatieDTO);
                            LocatieDTO managedLocatieDTO = locatieRepository.save(locatie);
                            omvatVastgesteld.add(managedLocatieDTO);
                        }
                    });
                }
            }
        }

        if (optionalOntwerpRegelingDTO.isEmpty()) {
            log.debug("+++> New OntwerpRegeling identificatie {} tijdstipRegistratie: {}, eindRegistratie: {} not exists. Saving ontwerpregeling.",
                    ontwerpRegelingDTO.getIdentificatie(),
                    ontwerpRegelingDTO.getGeregistreerdMet().getTijdstipRegistratie(),
                    ontwerpRegelingDTO.getGeregistreerdMet().getEindRegistratie());

            ontwerpRegelingDTO = oneToMany(ontwerpRegelingDTO);

            managedOntwerpRegelingDTO = ontwerpRegelingRepository.save(ontwerpRegelingDTO);

        } else {
            log.debug("---> Existing OntwerpRegeling identificatie {} tijdstipRegistratie: {}, eindRegistratie: {} exists. Skipping save for now. <---",
                    ontwerpRegelingDTO.getIdentificatie(),
                    ontwerpRegelingDTO.getGeregistreerdMet().getTijdstipRegistratie(),
                    ontwerpRegelingDTO.getGeregistreerdMet().getEindRegistratie());

            ontwerpRegelingDTO = oneToMany(optionalOntwerpRegelingDTO.get());
            managedOntwerpRegelingDTO = ontwerpRegelingRepository.save(ontwerpRegelingDTO);
        }

        return managedOntwerpRegelingDTO;
    }

    private OntwerpRegelingDTO oneToMany(OntwerpRegelingDTO ontwerpRegelingDTO) {
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
            Optional<SoortRegelingDTO> optionalSoortRegelingDTO = soortRegelingRepository.findByCode(ontwerpRegelingDTO.getType().getCode());
            SoortRegelingDTO managedSoortRegelingDTO;
            if (optionalSoortRegelingDTO.isEmpty()) {
                managedSoortRegelingDTO = soortRegelingRepository.save(ontwerpRegelingDTO.getType());
            } else {
                managedSoortRegelingDTO = optionalSoortRegelingDTO.get();
            }
            ontwerpRegelingDTO.setType(managedSoortRegelingDTO);
        }
        return ontwerpRegelingDTO;
    }
}
