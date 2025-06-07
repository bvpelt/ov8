package com.bsoft.ov8.loader.exceptions;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    // You might also want to add constructors for direct WebClientResponseException wrapping
    public OzonApiException(String message, WebClientResponseException cause) {
        super(message, cause);
        this.statusCode = cause.getStatusCode();
        this.errorDetails = cause.getResponseBodyAsString();
    }
}