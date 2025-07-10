package com.bsoft.ov8.loader;

import com.bsoft.ov8.loader.database.BevoegdGezagDTO;
import com.bsoft.ov8.loader.database.RegelingDTO;
import com.bsoft.ov8.loader.mappers.*;
import com.bsoft.ov8.loader.repositories.RegelingRepository;
import jakarta.transaction.Transactional;
import nl.overheid.omgevingswet.ozon.model.BevoegdGezag;
import nl.overheid.omgevingswet.ozon.model.Regeling;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional

public class RegelingMapperTest {

    @Autowired
    private RegelingMapper regelingMapper;

    @MockBean
    private RegelingRepository regelingRepository;

    @Autowired
    private BevoegdGezagMapper bevoegdGezagMapper;

    @Autowired
    private LocatieMapper locatieMapper;

    @Autowired
    private SoortRegelingMapper soortRegelingMapper;

    @Autowired
    private UriMapper uriMapper;

    @Test
    void testToBevoegdGezagDTO_nullInput() {
        // When
        BevoegdGezagDTO result = bevoegdGezagMapper.toBevoegdGezagDTO(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void testToBevoegdGezagDTO_basicMapping() {
        // Given
        BevoegdGezag source = new BevoegdGezag();
        source.setNaam("Test Naam");
        source.setBestuurslaag("Test Bestuurslaag");
        source.setCode("TEST123");

        // When
        BevoegdGezagDTO result = bevoegdGezagMapper.toBevoegdGezagDTO(source);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNaam()).isEqualTo("Test Naam");
        assertThat(result.getBestuurslaag()).isEqualTo("Test Bestuurslaag");
        assertThat(result.getCode()).isEqualTo("TEST123");
    }

    @Test
    void testToBevoegdGezagDTO_partialNullFields() {
        // Given
        BevoegdGezag source = new BevoegdGezag();
        source.setNaam("Test Naam");
        // bestuurslaag and code are null

        // When
        BevoegdGezagDTO result = bevoegdGezagMapper.toBevoegdGezagDTO(source);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNaam()).isEqualTo("Test Naam");
        assertThat(result.getBestuurslaag()).isNull();
        assertThat(result.getCode()).isNull();
    }

    @Test
    void testToRegelingDTO_basicMapping() {
        // Given
        Regeling source = createBasicRegeling();

        // When
        RegelingDTO result = regelingMapper.toRegelingDTO(source);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIdentificatie()).isEqualTo(source.getIdentificatie().toString());
        assertThat(result.getOfficieleTitel()).isEqualTo(source.getOfficieleTitel());
        assertThat(result.getCiteerTitel()).isEqualTo(source.getCiteerTitel());
        // Add more assertions for mapped fields
    }

    private Regeling createBasicRegeling() {
        Regeling regeling = new Regeling();
        regeling.setIdentificatie(URI.create("https://example.com/regeling/123"));
        regeling.setOfficieleTitel("Test Regeling");
        regeling.setCiteerTitel("Test Citeer");
        regeling.setOpschrift("Test Opschrift");

        // Add BevoegdGezag
        BevoegdGezag bevoegdGezag = new BevoegdGezag();
        bevoegdGezag.setNaam("Test Gezag");
        bevoegdGezag.setCode("TG123");
        regeling.setAangeleverdDoorEen(bevoegdGezag);

        return regeling;
    }

    @Test
    void testMapOpvolgerVan_simpleRecursion() {
        // Given
        Regeling parent = createBasicRegeling();
        Regeling child = createBasicRegeling();
        child.setIdentificatie(URI.create("https://example.com/regeling/456"));

        parent.setOpvolgerVan(List.of(child));

        // Mock repository to return empty (no existing entities)
        when(regelingRepository.findByIdentificatieAndTijdstipregistratieAndBegingeldigheid(
                anyString(), any(), any())).thenReturn(Optional.empty());

        // When
        RegelingDTO result = regelingMapper.toRegelingDTO(parent);

        // Then
        assertThat(result.getOpvolgerVan()).hasSize(1);
        RegelingDTO childDto = result.getOpvolgerVan().iterator().next();
        assertThat(childDto.getIdentificatie()).isEqualTo(child.getIdentificatie().toString());
    }

    @Test
    void testMapOpvolgerVan_existingEntity() {
        String opvolgerId = "https://example.com/regeling/opvolger";
        String existingId = "https://example.com/regeling/existing";

        // Given
        Regeling source = createBasicRegeling();
        Regeling opvolger = createBasicRegeling();
        opvolger.setIdentificatie(URI.create(opvolgerId));

        source.setOpvolgerVan(List.of(opvolger));

        // Mock existing entity in database
        RegelingDTO existingDto = new RegelingDTO();
        existingDto.setId(999L);
        existingDto.setIdentificatie(existingId);
        existingDto.setOfficieleTitel("Existing Title");

        when(regelingRepository.findByIdentificatieAndTijdstipregistratieAndBegingeldigheid(
                eq(existingId), any(), any()))
                .thenReturn(Optional.of(existingDto));

        // When
        RegelingDTO result = regelingMapper.toRegelingDTO(source);

        // Then
        assertThat(result.getOpvolgerVan()).hasSize(1);
        RegelingDTO opvolgerDto = result.getOpvolgerVan().iterator().next();
        assertThat(opvolgerDto.getIdentificatie()).isEqualTo(opvolgerId); // Should reuse existing entity
    }

    @Test
    void testToRegelingDTO_allFieldsMapped() {
        // Given - Create source with all possible fields set

        // Define the desired date format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        // Format the LocalDate to a String
        String inwerkingTotDate = LocalDate.now().format(formatter);
        String geldigTotDate = LocalDate.now().plusDays(30).format(formatter);

        Regeling source = new Regeling();
        source.setOfficieleTitel("Official Title");
        source.setCiteerTitel("Citeer Title");
        source.setOpschrift("Opschrift");
        source.setConditie("Conditie");
        source.setPublicatieID("PUB123");
        source.setInwerkingTot(inwerkingTotDate);
        source.setGeldigTot(geldigTotDate);
        
        // When
        RegelingDTO result = regelingMapper.toRegelingDTO(source);

        // Then - Assert all expected fields are mapped
        assertThat(result.getOfficieleTitel()).isEqualTo(source.getOfficieleTitel());
        assertThat(result.getCiteerTitel()).isEqualTo(source.getCiteerTitel());
        assertThat(result.getOpschrift()).isEqualTo(source.getOpschrift());
        assertThat(result.getConditie()).isEqualTo(source.getConditie());
        assertThat(result.getPublicatieID()).isEqualTo(source.getPublicatieID());
        assertThat(result.getInwerkingTot()).isEqualTo(source.getInwerkingTot());
        assertThat(result.getGeldigTot()).isEqualTo(source.getGeldigTot());
    }

}
