package com.franchises.infrastructure.adapter.in.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.franchises.domain.exception.ConcurrencyConflictException;
import com.franchises.domain.exception.DuplicateResourceException;
import com.franchises.domain.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalErrorHandlerTest {

    private final GlobalErrorHandler handler = new GlobalErrorHandler(new ObjectMapper());

    private MockServerWebExchange handle(GlobalErrorHandler errorHandler, Throwable error) {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/franchises"));
        errorHandler.handle(exchange, error).block();
        return exchange;
    }

    @Test
    @DisplayName("NotFoundException se traduce a 404 con el mensaje del dominio")
    void notFound() {
        MockServerWebExchange exchange = handle(handler, new NotFoundException("Franquicia no encontrada: f-1"));

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exchange.getResponse().getBodyAsString().block())
                .contains("Franquicia no encontrada: f-1");
    }

    @Test
    @DisplayName("DuplicateResourceException se traduce a 409")
    void conflict() {
        MockServerWebExchange exchange = handle(handler, new DuplicateResourceException("duplicado"));

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("ConcurrencyConflictException se traduce a 409")
    void concurrencyConflict() {
        MockServerWebExchange exchange = handle(handler,
                new ConcurrencyConflictException("modificada concurrentemente", new RuntimeException()));

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exchange.getResponse().getBodyAsString().block())
                .contains("modificada concurrentemente");
    }

    @Test
    @DisplayName("IllegalArgumentException se traduce a 400")
    void badRequest() {
        MockServerWebExchange exchange = handle(handler, new IllegalArgumentException("dato inválido"));

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("ServerWebInputException conserva su razón en el mensaje")
    void serverWebInput() {
        MockServerWebExchange exchange = handle(handler, new ServerWebInputException("cuerpo obligatorio"));

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("cuerpo obligatorio");
    }

    @Test
    @DisplayName("ResponseStatusException sin razón usa la descripción del estado")
    void responseStatusWithoutReason() {
        MockServerWebExchange exchange = handle(handler,
                new ResponseStatusException(HttpStatus.NOT_FOUND));

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("Not Found");
    }

    @Test
    @DisplayName("una excepción inesperada y sin mensaje se traduce a 500 con mensaje por defecto")
    void internalError() {
        MockServerWebExchange exchange = handle(handler, new IllegalStateException());

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("Internal Server Error");
    }

    @Test
    @DisplayName("si la serialización JSON falla se escribe un cuerpo mínimo")
    void jsonSerializationFailure() throws JsonProcessingException {
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        when(failingMapper.writeValueAsBytes(any())).thenThrow(new JsonProcessingException("boom") {
        });

        MockServerWebExchange exchange = handle(new GlobalErrorHandler(failingMapper),
                new NotFoundException("no existe"));

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exchange.getResponse().getBodyAsString().block()).isEqualTo("{\"status\":404}");
    }
}
