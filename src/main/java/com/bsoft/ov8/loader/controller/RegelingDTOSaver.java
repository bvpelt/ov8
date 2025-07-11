package com.bsoft.ov8.loader.controller;

import com.bsoft.ov8.loader.database.BevoegdGezagDTO;
import com.bsoft.ov8.loader.database.LocatieDTO;
import com.bsoft.ov8.loader.database.RegelingDTO;
import com.bsoft.ov8.loader.database.SoortRegelingDTO;
import com.bsoft.ov8.loader.mappers.LocatieMapper;
import com.bsoft.ov8.loader.mappers.RegelingMapper;
import com.bsoft.ov8.loader.repositories.BevoegdGezagRepository;
import com.bsoft.ov8.loader.repositories.LocatieRepository;
import com.bsoft.ov8.loader.repositories.RegelingRepository;
import com.bsoft.ov8.loader.repositories.SoortRegelingRepository;
import lombok.extern.slf4j.Slf4j;
import nl.overheid.omgevingswet.ozon.model.EmbeddedLocatie;
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
public class RegelingDTOSaver {
    private final LocatieMapper locatieMapper;
    private final RegelingMapper regelingMapper;

    private final BevoegdGezagRepository bevoegdGezagRepository;
    private final RegelingRepository regelingRepository;
    private final SoortRegelingRepository soortRegelingRepository;
    private final LocatieRepository locatieRepository;

    public RegelingDTOSaver(BevoegdGezagRepository bevoegdGezagRepository,
                            RegelingRepository regelingRepository,
                            SoortRegelingRepository soortRegelingRepository,
                            LocatieRepository locatieRepository,
                            LocatieMapper locatieMapper,
                            RegelingMapper regelingMapper) {
        this.bevoegdGezagRepository = bevoegdGezagRepository;
        this.regelingRepository = regelingRepository;
        this.soortRegelingRepository = soortRegelingRepository;
        this.locatieRepository = locatieRepository;
        this.locatieMapper = locatieMapper;
        this.regelingMapper = regelingMapper;
    }

    @Transactional
    public RegelingDTO saveRegeling(RegelingDTO regelingDTO, Regeling regeling) {
        log.debug("===> Regeling identificatie {} tijdstipRegistratie: {}, beginGeldigheid: {}.",
                regelingDTO.getIdentificatie(),
                regelingDTO.getRegistratiegegevens().getTijdstipRegistratie(),
                regelingDTO.getRegistratiegegevens().getBeginGeldigheid());

        RegelingDTO savedRegelingDTO;

        oneToMany(regelingDTO);

        // Check if RegelingDTO already exists
        Optional<RegelingDTO> optionalRegelingDTO = regelingRepository.findByIdentificatieAndTijdstipregistratieAndBegingeldigheid(
                regelingDTO.getIdentificatie(),
                regelingDTO.getRegistratiegegevens().getTijdstipRegistratie(),
                regelingDTO.getRegistratiegegevens().getBeginGeldigheid());

        if (optionalRegelingDTO.isEmpty()) {
            log.debug("+++> New Regeling identificatie {} tijdstipRegistratie: {}, beginGeldigheid: {} not exists. Saving regeling.",
                    regelingDTO.getIdentificatie(),
                    regelingDTO.getRegistratiegegevens().getTijdstipRegistratie(),
                    regelingDTO.getRegistratiegegevens().getBeginGeldigheid());

            savedRegelingDTO = regelingRepository.save(regelingDTO);

            manyToOne(regelingDTO, regeling);

            // --- Save the RegelingDTO ---
            // Hibernate will now correctly manage the many-to-many relationship
            // based on the managed entities in regelingDTO.regelingsgebied and the cascade type.
            regelingRepository.save(regelingDTO);
        } else {
            log.debug("---> Existing Regeling identificatie {} tijdstipRegistratie: {}, beginGeldigheid: {} exists. Skipping save for now. <---",
                    regelingDTO.getIdentificatie(),
                    regelingDTO.getRegistratiegegevens().getTijdstipRegistratie(),
                    regelingDTO.getRegistratiegegevens().getBeginGeldigheid());

            manyToOne(regelingDTO, regeling);
            savedRegelingDTO = regelingRepository.save(regelingDTO);
        }

        return savedRegelingDTO;
    }

    private void oneToMany(RegelingDTO regelingDTO) {
        // --- Handle BevoegdGezagDTO and SoortRegelingDTO (similar logic as before) ---
        if (regelingDTO.getBevoegdGezag() != null) {
            Optional<BevoegdGezagDTO> optionalBevoegdGezagDTO = bevoegdGezagRepository.findByCode(regelingDTO.getBevoegdGezag().getCode());
            BevoegdGezagDTO managedBevoegdGezagDTO;
            if (optionalBevoegdGezagDTO.isEmpty()) {
                managedBevoegdGezagDTO = bevoegdGezagRepository.save(regelingDTO.getBevoegdGezag());
            } else {
                managedBevoegdGezagDTO = optionalBevoegdGezagDTO.get();
            }
            regelingDTO.setBevoegdGezag(managedBevoegdGezagDTO);
        }

        if (regelingDTO.getType() != null) {
            Optional<SoortRegelingDTO> optionalSoortRegelingDTO = soortRegelingRepository.findByCode(regelingDTO.getType().getCode());
            SoortRegelingDTO managedSoortRegelingDTO;
            if (optionalSoortRegelingDTO.isEmpty()) {
                managedSoortRegelingDTO = soortRegelingRepository.save(regelingDTO.getType());
            } else {
                managedSoortRegelingDTO = optionalSoortRegelingDTO.get();
            }
            regelingDTO.setType(managedSoortRegelingDTO);
        }
    }

    private void manyToOne(RegelingDTO regelingDTO, Regeling regeling) {
        //
        // --- Prepare RelatieDTO's for Many-to-Many relationship opvolgervan ---
        Set<RegelingDTO> managedOpvolgerForRegeling = new HashSet<>();

        if (regeling.getOpvolgerVan() != null && !regeling.getOpvolgerVan().isEmpty()) {
            // Iterate over the Regeling mapped from the API response
            for (Regeling currentOpvolgerInRegeling : regeling.getOpvolgerVan()) {
                Optional<RegelingDTO> optionalOpvolgerVanDTO = regelingRepository.findByIdentificatieAndTijdstipregistratieAndBegingeldigheid(currentOpvolgerInRegeling.getIdentificatie().toString(),
                        currentOpvolgerInRegeling.getGeregistreerdMet().getTijdstipRegistratie(),
                        currentOpvolgerInRegeling.getGeregistreerdMet().getBeginGeldigheid());

                RegelingDTO managedOpvolgerVanDTO;
                if (optionalOpvolgerVanDTO.isPresent()) {
                    // If LocatieDTO already exists, use the one from the database directly.
                    // It is already a managed entity within this transaction.
                    managedOpvolgerVanDTO = optionalOpvolgerVanDTO.get();
                } else {
                    // If RegelingDTO does not exist, save the new one to make it managed.
                    RegelingDTO opvolgerDTO = regelingMapper.toRegelingDTO(currentOpvolgerInRegeling);
                    managedOpvolgerVanDTO = regelingRepository.save(opvolgerDTO);
                }
                managedOpvolgerForRegeling.add(managedOpvolgerVanDTO);
            }
        }
        // Set the collection of managed RelatieDTO on the RegelingDTO (the owning side)
        regelingDTO.setOpvolgerVan(managedOpvolgerForRegeling);

        // --- Prepare LocatieDTOs for Many-to-Many relationship regelingsgebied ---
        Set<LocatieDTO> managedLocatiesForRegeling = getLocatieDTOS(regelingDTO, regeling);
        regelingDTO.setRegelingsgebied(managedLocatiesForRegeling);
    }

    private Set<LocatieDTO> getLocatieDTOS(RegelingDTO regelingDTO, Regeling regeling) {
        Set<LocatieDTO> managedLocatiesForRegeling = new HashSet<>();
        RegelingAllOfEmbedded regelingAllOfEmbedded = regeling.getEmbedded();
        if (regelingAllOfEmbedded != null) {
            EmbeddedLocatie embeddedLocatie = regelingAllOfEmbedded.getRegelingsgebied();
            if (embeddedLocatie != null) {

                Optional<LocatieDTO> optionalLocatieDTO = locatieRepository.findByIdentificatieAndGeometrieIdentificatie(
                        embeddedLocatie.getIdentificatie().toString(),
                        embeddedLocatie.getGeometrieIdentificatie());

                LocatieDTO managedLocatieDTO;
                if (optionalLocatieDTO.isPresent()) { // If LocatieDTO already exists, use the one from the database directly.
                    // It is already a managed entity within this transaction.
                    managedLocatieDTO = optionalLocatieDTO.get();
                } else { // If LocatieDTO does not exist, save the new one to make it managed.
                    LocatieDTO locatieDTO = locatieMapper.toLocatieDTO(embeddedLocatie);

                    managedLocatieDTO = locatieRepository.save(locatieDTO);

                    Set<LocatieDTO> managedEmbeddedLocatieDTO = new HashSet<>();

                    // Een gebiedengroep omvat 1..n locaties
                    if (locatieDTO.getLocatieType().getValue().equals("Gebiedengroep")) {
                        List<EmbeddedLocatie> omvat = embeddedLocatie.getEmbedded().getOmvat();
                        log.debug("Gebieden omvat grootte: {}, parent: {}, regeling: {}", omvat.size(), locatieDTO.getIdentificatie(), regelingDTO.getIdentificatie());
                        omvat.forEach(gebied -> {

                            Optional<LocatieDTO> optionalGebiedDTO = locatieRepository.findByIdentificatieAndGeometrieIdentificatie(
                                    gebied.getIdentificatie().toString(),
                                    gebied.getGeometrieIdentificatie());

                            if (optionalGebiedDTO.isPresent()) {
                                LocatieDTO foundGebiedDTO = optionalGebiedDTO.get();
                                managedLocatieDTO.addMember(foundGebiedDTO);
                            } else {
                                LocatieDTO gebiedDTO = locatieMapper.toLocatieDTO(gebied);

                                LocatieDTO managedOmvatDTO = locatieRepository.save(gebiedDTO);
                                log.debug("Gebiedengroep insert: {} {}, gebied: {} {}", managedLocatieDTO.getId(), managedLocatieDTO.getIdentificatie(), managedOmvatDTO.getId(), managedOmvatDTO.getIdentificatie());

                                managedLocatieDTO.addMember(managedOmvatDTO);
                            }
                        });
                        locatieRepository.save(managedLocatieDTO);
                    }
                }
                managedLocatiesForRegeling.add(managedLocatieDTO);
            }
        }
        return managedLocatiesForRegeling;
    }

}
