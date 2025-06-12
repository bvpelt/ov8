package com.bsoft.ov8.loader.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.net.URI;

@Mapper(componentModel = "spring") // No need for unmappedTargetPolicy here
@Component // Make it a Spring component so MapStruct can inject it
public class UriMapper {

    @Named("mapUriToString") // Still use @Named to refer to it
    public String mapUriToString(URI uri) {
        return uri != null ? uri.toString() : null;
    }

    @Named("mapStringToUri")
    public URI mapStringToUri(String s) {
        return s != null ? URI.create(s) : null;
    }
}