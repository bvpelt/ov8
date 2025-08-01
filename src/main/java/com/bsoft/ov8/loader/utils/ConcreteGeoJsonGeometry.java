package com.bsoft.ov8.loader.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConcreteGeoJsonGeometry {
    @JsonProperty("type")
    private String type;

    @JsonProperty("coordinates")
    private Object coordinates;

    // Default constructor
    public ConcreteGeoJsonGeometry() {
    }

    // Getters and setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Object coordinates) {
        this.coordinates = coordinates;
    }
}
