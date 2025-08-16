package com.bsoft.ov8.loader.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
// Simple result class for JSON responses
public class ProcessedRegelingResult {
    private String identification;
    private String status;
    private String message;
    private long procestime;

}