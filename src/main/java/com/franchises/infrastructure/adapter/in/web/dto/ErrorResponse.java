package com.franchises.infrastructure.adapter.in.web.dto;

public record ErrorResponse(int status, String error, String message) {
}
