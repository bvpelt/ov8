package com.bsoft.ov8.loader;

import com.bsoft.ov8.loader.database.BevoegdGezagDTO;
import com.bsoft.ov8.loader.database.RegelingDTO;
import nl.overheid.omgevingswet.ozon.model.BevoegdGezag;
import nl.overheid.omgevingswet.ozon.model.Regeling;

import java.net.URI;
import java.util.UUID;

public class RegelingTestDataFactory {

    public static Regeling aRegeling() {
        Regeling regeling = new Regeling();
        regeling.setIdentificatie(URI.create("https://example.com/regeling/" + UUID.randomUUID()));
        regeling.setOfficieleTitel("Test Regeling");
        regeling.setCiteerTitel("Test Citeer");
        regeling.setOpschrift("Test Opschrift");
        regeling.setAangeleverdDoorEen(aBevoegdGezag());
        return regeling;
    }

    public static BevoegdGezag aBevoegdGezag() {
        BevoegdGezag bevoegdGezag = new BevoegdGezag();
        bevoegdGezag.setNaam("Test Gezag");
        bevoegdGezag.setCode("TG" + System.currentTimeMillis());
        bevoegdGezag.setBestuurslaag("Test Bestuurslaag");
        return bevoegdGezag;
    }

    public static RegelingDTO aRegelingDTO() {
        RegelingDTO regelingDTO = new RegelingDTO();
        regelingDTO.setIdentificatie("https://example.com/regeling/" + UUID.randomUUID());
        regelingDTO.setOfficieleTitel("Test Regeling DTO");
        regelingDTO.setCiteerTitel("Test Citeer DTO");
        return regelingDTO;
    }

    public static BevoegdGezagDTO aBevoegdGezagDTO() {
        BevoegdGezagDTO bevoegdGezagDTO = new BevoegdGezagDTO();
        bevoegdGezagDTO.setNaam("Test Gezag DTO");
        bevoegdGezagDTO.setCode("TG" + System.currentTimeMillis());
        bevoegdGezagDTO.setBestuurslaag("Test Bestuurslaag DTO");
        return bevoegdGezagDTO;
    }
}
