package com.bsoft.ov8.loader.mappers;

import com.bsoft.ov8.loader.database.RegelingDTO;
import com.bsoft.ov8.loader.repositories.RegelingRepository;
import nl.overheid.omgevingswet.ozon.model.Regeling;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {BevoegdGezagMapper.class, SoortRegelingMapper.class} // IMPORTANT: Tell MapStruct to use BevoegdGezagMapper
)

@Component
public abstract class RegelingMapper {

    // Used to break recursion by keeping track of mapped entities during a single mapping operation
    // This map ensures that if RegelingA points to RegelingB, and RegelingB points back to RegelingA,
    // we don't end up in an infinite loop.
    private final ThreadLocal<java.util.Map<URI, RegelingDTO>> context = ThreadLocal.withInitial(java.util.HashMap::new);
    @Autowired
    private RegelingRepository regelingRepository; //

    // This method defines the mapping from Regeling to RegelingDTO
    @Mapping(source = "identificatie", target = "identificatie", qualifiedByName = "mapUriToString")
    @Mapping(target = "officieleTitel", source = "officieleTitel")
    @Mapping(source = "aangeleverdDoorEen", target = "bevoegdGezag")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "geregistreerdMet.versie", target = "versie")
    @Mapping(source = "geregistreerdMet.beginInwerking", target = "beginInwerking")
    @Mapping(source = "geregistreerdMet.beginGeldigheid", target = "beginGeldigheid")
    @Mapping(source = "geregistreerdMet.eindGeldigheid", target = "eindGeldigheid")
    @Mapping(source = "geregistreerdMet.tijdstipRegistratie", target = "tijdstipRegistratie")
    @Mapping(source = "geregistreerdMet.eindRegistratie", target = "eindRegistratie")
    @Mapping(target = "citeerTitel", source = "citeerTitel")
    @Mapping(target = "opschrift", source = "opschrift")
    @Mapping(target = "conditie", source = "conditie")
    @Mapping(target = "opvolgerVan", source = "opvolgerVan", qualifiedByName = "mapOpvolgerVan")
    // Custom mapping for recursion
    @Mapping(target = "heeftBijlagen", source = "heeftBijlagen")
    @Mapping(target = "heeftToelichtingen", source = "heeftToelichtingen")
    @Mapping(target = "publicatieID", source = "publicatieID")
    @Mapping(target = "inwerkingTot", source = "inwerkingTot")
    @Mapping(target = "geldigTot", source = "geldigTot")
    public abstract RegelingDTO toRegelingDTO(Regeling regeling);

    // Custom mapping method for 'aangeleverdDoorEen' to a Set<BevoegdGezagDTO>
    // URI to String converter
    @Named("mapUriToString")
    protected String mapUriToString(URI uri) {
        return uri != null ? uri.toString() : null;
    }

    // String to URI converter (if mapping back from DTO to API model)
    @Named("mapStringToUri")
    protected URI mapStringToUri(String s) {
        return s != null ? URI.create(s) : null;
    }

    // --- Custom Mapping for opvolgerVan to handle recursion and entity lookup ---
    @Named("mapOpvolgerVan")
    public Set<RegelingDTO> mapOpvolgerVan(List<Regeling> apiOpvolgerVanList) {
        if (apiOpvolgerVanList == null) {
            return Collections.emptySet();
        }

        // Initialize context for this mapping operation if it's the root call
        java.util.Map<URI, RegelingDTO> currentContext = context.get();
        if (currentContext.isEmpty()) { // This means it's the start of a new mapping session
            context.set(new java.util.HashMap<>()); // Re-initialize for safety
        }

        Set<RegelingDTO> dtoList = apiOpvolgerVanList.stream()
                .filter(Objects::nonNull)
                .map(apiRegeling -> {
                    // Check if this Regeling has already been mapped in the current context
                    if (currentContext.containsKey(apiRegeling.getIdentificatie())) {
                        return currentContext.get(apiRegeling.getIdentificatie()); // Return existing DTO to break recursion
                    }

                    // Attempt to find the RegelingDTO in the database by its unique identifier
                    // Assuming 'identificatie' is unique and corresponds to RegelingDTO.identificatie
                    // It's good practice to convert URI to String here as your DTO stores it as String
                    RegelingDTO existingDto = //regelingRepository.findByIdentificatie(apiRegeling.getIdentificatie().toString())
                            regelingRepository.findByIdentificatieAndTijdstipRegistratieAndBeginGeldigheid(apiRegeling.getIdentificatie().toString(),
                                            apiRegeling.getGeregistreerdMet().getTijdstipRegistratie(), apiRegeling.getGeregistreerdMet().getBeginGeldigheid())
                                    .orElse(null);

                    if (existingDto != null) {
                        // If entity exists, put it in context and update its properties
                        currentContext.put(apiRegeling.getIdentificatie(), existingDto);
                        // Recursively call a method to update the existing DTO from the API model
                        updateRegelingDTOFromApi(apiRegeling, existingDto);
                        return existingDto;
                    } else {
                        // If entity does not exist, create a new one
                        RegelingDTO newDto = new RegelingDTO();
                        // Place a "stub" in the context immediately to prevent infinite loop for deeply nested/recursive calls
                        currentContext.put(apiRegeling.getIdentificatie(), newDto);
                        // Recursively map its properties, including its own opvolgerVan
                        // Call the *abstract* mapper method to perform the full mapping
                        // This uses the same `toRegelingDTO` logic but on the nested object
                        mapInternalRegelingToRegelingDTO(apiRegeling, newDto);
                        return newDto;
                    }
                })
                .collect(Collectors.toSet());

        // Clear the context for this thread once the root mapping operation is complete
        // This is important to prevent memory leaks and ensure fresh context for subsequent mappings
        if (currentContext.isEmpty()) { // Only clear if this is the root call
            context.remove();
        }

        return dtoList;
    }

    // Helper method to perform the mapping of individual Regeling (API) to RegelingDTO (DB)
    // This is abstract and will be implemented by MapStruct.
    // This is used for mapping nested Regeling objects within opvolgerVan
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "identificatie", source = "identificatie", qualifiedByName = "mapUriToString")
    @Mapping(target = "bevoegdGezag", source = "aangeleverdDoorEen")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "opvolgerVan", source = "opvolgerVan", qualifiedByName = "mapOpvolgerVan") // Recursive call!
    @Mapping(target = "versie", source = "geregistreerdMet.versie")
    @Mapping(target = "beginInwerking", source = "geregistreerdMet.beginInwerking")
    @Mapping(target = "beginGeldigheid", source = "geregistreerdMet.beginGeldigheid")
    @Mapping(target = "eindGeldigheid", source = "geregistreerdMet.eindGeldigheid")
    @Mapping(target = "tijdstipRegistratie", source = "geregistreerdMet.tijdstipRegistratie")
    @Mapping(target = "eindRegistratie", source = "geregistreerdMet.eindRegistratie")
    protected abstract void mapInternalRegelingToRegelingDTO(Regeling apiRegeling, @MappingTarget RegelingDTO target);


    // Helper method to update an existing RegelingDTO from an API Regeling
    // This is used when an entity is found by its identificatie.
    @Mapping(target = "id", ignore = true) // Don't update ID
    @Mapping(target = "identificatie", source = "identificatie", qualifiedByName = "mapUriToString")
    @Mapping(target = "bevoegdGezag", source = "aangeleverdDoorEen")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "opvolgerVan", source = "opvolgerVan", qualifiedByName = "mapOpvolgerVan")
    @Mapping(target = "versie", source = "geregistreerdMet.versie")
    @Mapping(target = "beginInwerking", source = "geregistreerdMet.beginInwerking")
    @Mapping(target = "beginGeldigheid", source = "geregistreerdMet.beginGeldigheid")
    @Mapping(target = "eindGeldigheid", source = "geregistreerdMet.eindGeldigheid")
    @Mapping(target = "tijdstipRegistratie", source = "geregistreerdMet.tijdstipRegistratie")
    @Mapping(target = "eindRegistratie", source = "geregistreerdMet.eindRegistratie")
    public abstract void updateRegelingDTOFromApi(Regeling apiRegeling, @MappingTarget RegelingDTO target);

}