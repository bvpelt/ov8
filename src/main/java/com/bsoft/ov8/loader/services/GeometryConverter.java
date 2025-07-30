package com.bsoft.ov8.loader.services;// Or wherever you want to place this utility

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import nl.overheid.omgevingswet.ozon.geodownload.model.GeoJsonGeometry;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class GeometryConverter {

    private final GeometryFactory geometryFactory;
    private final ObjectMapper objectMapper;

    public GeometryConverter() {
        // SRID 28992 for RD_New (Rijksdriehoekmeting)
        this.geometryFactory = new GeometryFactory(new PrecisionModel(), 28992);
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Converts a GeoJSON string to a JTS Geometry object.
     */
    public Geometry convertGeoJsonToJtsGeometry(String geoJsonString) {
        if (geoJsonString == null || geoJsonString.trim().isEmpty()) {
            log.warn("Attempted to convert null or empty GeoJSON string to JTS Geometry.");
            return null;
        }

        try {
            JsonNode geoJsonNode = objectMapper.readTree(geoJsonString);
            return parseGeometry(geoJsonNode);
        } catch (Exception e) {
            log.error("Failed to convert GeoJSON string to JTS Geometry: {}", geoJsonString, e);
            return null;
        }
    }

    public Geometry convertGeoJsonPojoToJtsGeometry(GeoJsonGeometry geoJsonPojo) {
        if (geoJsonPojo == null) {
            return null;
        }

        try {
            // Convert the POJO back to JSON string
            String geoJsonString = objectMapper.writeValueAsString(geoJsonPojo);
            return convertGeoJsonToJtsGeometry(geoJsonString);
        } catch (Exception e) {
            log.error("Failed to convert GeoJsonGeometry POJO to JTS Geometry", e);
            return null;
        }
    }

    private Geometry parseGeometry(JsonNode geoJsonNode) {
        String type = geoJsonNode.get("type").asText();
        JsonNode coordinates = geoJsonNode.get("coordinates");

        return switch (type.toLowerCase()) {
            case "point" -> createPoint(coordinates);
            case "linestring" -> createLineString(coordinates);
            case "polygon" -> createPolygon(coordinates);
            case "multipoint" -> createMultiPoint(coordinates);
            case "multilinestring" -> createMultiLineString(coordinates);
            case "multipolygon" -> createMultiPolygon(coordinates);
            case "geometrycollection" -> createGeometryCollection(geoJsonNode.get("geometries"));
            default -> {
                log.warn("Unsupported geometry type: {}", type);
                yield null;
            }
        };
    }

    private Point createPoint(JsonNode coordinates) {
        double x = coordinates.get(0).asDouble();
        double y = coordinates.get(1).asDouble();
        return geometryFactory.createPoint(new Coordinate(x, y));
    }

    private LineString createLineString(JsonNode coordinates) {
        Coordinate[] coords = parseCoordinateArray(coordinates);
        return geometryFactory.createLineString(coords);
    }

    private Polygon createPolygon(JsonNode coordinates) {
        // First array is exterior ring
        LinearRing exterior = geometryFactory.createLinearRing(parseCoordinateArray(coordinates.get(0)));

        // Remaining arrays are holes
        LinearRing[] holes = new LinearRing[coordinates.size() - 1];
        for (int i = 1; i < coordinates.size(); i++) {
            holes[i - 1] = geometryFactory.createLinearRing(parseCoordinateArray(coordinates.get(i)));
        }

        return geometryFactory.createPolygon(exterior, holes);
    }

    private MultiPoint createMultiPoint(JsonNode coordinates) {
        Point[] points = new Point[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = createPoint(coordinates.get(i));
        }
        return geometryFactory.createMultiPoint(points);
    }

    private MultiLineString createMultiLineString(JsonNode coordinates) {
        LineString[] lineStrings = new LineString[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            lineStrings[i] = createLineString(coordinates.get(i));
        }
        return geometryFactory.createMultiLineString(lineStrings);
    }

    private MultiPolygon createMultiPolygon(JsonNode coordinates) {
        Polygon[] polygons = new Polygon[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            polygons[i] = createPolygon(coordinates.get(i));
        }
        return geometryFactory.createMultiPolygon(polygons);
    }

    private GeometryCollection createGeometryCollection(JsonNode geometries) {
        List<Geometry> geomList = new ArrayList<>();
        for (JsonNode geomNode : geometries) {
            Geometry geom = parseGeometry(geomNode);
            if (geom != null) {
                geomList.add(geom);
            }
        }
        return geometryFactory.createGeometryCollection(geomList.toArray(new Geometry[0]));
    }

    private Coordinate[] parseCoordinateArray(JsonNode coordinates) {
        Coordinate[] coords = new Coordinate[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            JsonNode coord = coordinates.get(i);
            double x = coord.get(0).asDouble();
            double y = coord.get(1).asDouble();
            coords[i] = new Coordinate(x, y);
        }
        return coords;
    }
}