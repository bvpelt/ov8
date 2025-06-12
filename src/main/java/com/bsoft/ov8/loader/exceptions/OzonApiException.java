package com.bsoft.ov8.loader.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Getter
public class OzonApiException extends RuntimeException {
    private final HttpStatusCode statusCode;
    private final String errorDetails; // Or more specific objects like Problem/Problem400

    public OzonApiException(String message, HttpStatusCode statusCode, String errorDetails, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorDetails = errorDetails;
    }

    public OzonApiException(String message, HttpStatusCode statusCode, String errorDetails) {
        super(message);
        this.statusCode = statusCode;
        this.errorDetails = errorDetails;
    }

    // You might also want to add constructors for direct WebClientResponseException wrapping
    public OzonApiException(String message, WebClientResponseException cause) {
        super(message, cause);
        this.statusCode = cause.getStatusCode();
        this.errorDetails = cause.getResponseBodyAsString();
    }

}