package com.franchises.infrastructure.adapter.in.web;

import com.franchises.infrastructure.adapter.in.web.dto.BranchRequest;
import com.franchises.infrastructure.adapter.in.web.dto.ErrorResponse;
import com.franchises.infrastructure.adapter.in.web.dto.FranchiseRequest;
import com.franchises.infrastructure.adapter.in.web.dto.FranchiseResponse;
import com.franchises.infrastructure.adapter.in.web.dto.NameUpdateRequest;
import com.franchises.infrastructure.adapter.in.web.dto.ProductRequest;
import com.franchises.infrastructure.adapter.in.web.dto.StockUpdateRequest;
import com.franchises.infrastructure.adapter.in.web.dto.TopStockProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.PATCH;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * Definición funcional de las rutas del API.
 *
 * <p>La documentación OpenAPI/Swagger de cada ruta se declara explícitamente con
 * {@code @RouterOperation}, ya que springdoc no puede inferirla automáticamente de un
 * {@link RouterFunction} funcional (a diferencia de un {@code @RestController} anotado).
 */
@Configuration
@Tag(name = "Franchises", description = "Gestión de franquicias, sucursales y productos")
public class FranchiseRouter {

    private static final String NOT_FOUND = "Franquicia, sucursal o producto no encontrado";
    private static final String CONFLICT = "Ya existe una sucursal o producto con ese nombre";
    private static final String BAD_REQUEST = "Cuerpo de la petición inválido (nombre en blanco, stock negativo, sin cuerpo)";

    @Bean
    @RouterOperations({
            @RouterOperation(path = "/api/franchises", method = RequestMethod.POST,
                    beanClass = FranchiseHandler.class, beanMethod = "createFranchise",
                    operation = @Operation(operationId = "createFranchise", summary = "Crear una franquicia",
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema(implementation = FranchiseRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Franquicia creada",
                                            content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "400", description = BAD_REQUEST,
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = "/api/franchises/{franchiseId}/name", method = RequestMethod.PATCH,
                    beanClass = FranchiseHandler.class, beanMethod = "updateFranchiseName",
                    operation = @Operation(operationId = "updateFranchiseName",
                            summary = "Actualizar el nombre de una franquicia",
                            parameters = @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema(implementation = NameUpdateRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Franquicia actualizada",
                                            content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "404", description = NOT_FOUND,
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "400", description = BAD_REQUEST,
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = "/api/franchises/{franchiseId}/top-stock-products", method = RequestMethod.GET,
                    beanClass = FranchiseHandler.class, beanMethod = "topStockProducts",
                    operation = @Operation(operationId = "topStockProducts",
                            summary = "Producto con más stock por sucursal de la franquicia",
                            parameters = @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                            responses = {
                                    @ApiResponse(responseCode = "200",
                                            description = "Listado con el producto de mayor stock de cada sucursal",
                                            content = @Content(array = @ArraySchema(
                                                    schema = @Schema(implementation = TopStockProductResponse.class)))),
                                    @ApiResponse(responseCode = "404", description = NOT_FOUND,
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = "/api/franchises/{franchiseId}/branches", method = RequestMethod.POST,
                    beanClass = FranchiseHandler.class, beanMethod = "addBranch",
                    operation = @Operation(operationId = "addBranch", summary = "Agregar una sucursal a la franquicia",
                            parameters = @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema(implementation = BranchRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Sucursal agregada",
                                            content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "404", description = NOT_FOUND,
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = CONFLICT,
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = "/api/franchises/{franchiseId}/branches/{branchId}/name",
                    method = RequestMethod.PATCH,
                    beanClass = FranchiseHandler.class, beanMethod = "updateBranchName",
                    operation = @Operation(operationId = "updateBranchName",
                            summary = "Actualizar el nombre de una sucursal",
                            parameters = {
                                    @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "branchId", in = ParameterIn.PATH, required = true)
                            },
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema(implementation = NameUpdateRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Sucursal actualizada",
                                            content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "404", description = NOT_FOUND,
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = CONFLICT,
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = "/api/franchises/{franchiseId}/branches/{branchId}/products",
                    method = RequestMethod.POST,
                    beanClass = FranchiseHandler.class, beanMethod = "addProduct",
                    operation = @Operation(operationId = "addProduct", summary = "Agregar un producto a la sucursal",
                            parameters = {
                                    @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "branchId", in = ParameterIn.PATH, required = true)
                            },
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema(implementation = ProductRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Producto agregado",
                                            content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "404", description = NOT_FOUND,
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = CONFLICT,
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = "/api/franchises/{franchiseId}/branches/{branchId}/products/{productId}",
                    method = RequestMethod.DELETE,
                    beanClass = FranchiseHandler.class, beanMethod = "removeProduct",
                    operation = @Operation(operationId = "removeProduct", summary = "Eliminar un producto de la sucursal",
                            parameters = {
                                    @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "branchId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "productId", in = ParameterIn.PATH, required = true)
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Producto eliminado",
                                            content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "404", description = NOT_FOUND,
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = "/api/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock",
                    method = RequestMethod.PATCH,
                    beanClass = FranchiseHandler.class, beanMethod = "updateProductStock",
                    operation = @Operation(operationId = "updateProductStock", summary = "Modificar el stock de un producto",
                            parameters = {
                                    @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "branchId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "productId", in = ParameterIn.PATH, required = true)
                            },
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema(implementation = StockUpdateRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Stock actualizado",
                                            content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "404", description = NOT_FOUND,
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "400", description = BAD_REQUEST,
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = "/api/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name",
                    method = RequestMethod.PATCH,
                    beanClass = FranchiseHandler.class, beanMethod = "updateProductName",
                    operation = @Operation(operationId = "updateProductName", summary = "Actualizar el nombre de un producto",
                            parameters = {
                                    @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "branchId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "productId", in = ParameterIn.PATH, required = true)
                            },
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema(implementation = NameUpdateRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Producto actualizado",
                                            content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "404", description = NOT_FOUND,
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = CONFLICT,
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            }))
    })
    public RouterFunction<ServerResponse> franchiseRoutes(FranchiseHandler handler) {
        return nest(path("/api/franchises"),
                route(POST(""), handler::createFranchise)
                        .andRoute(PATCH("/{franchiseId}/name"), handler::updateFranchiseName)
                        .andRoute(GET("/{franchiseId}/top-stock-products"), handler::topStockProducts)
                        .andRoute(POST("/{franchiseId}/branches"), handler::addBranch)
                        .andRoute(PATCH("/{franchiseId}/branches/{branchId}/name"), handler::updateBranchName)
                        .andRoute(POST("/{franchiseId}/branches/{branchId}/products"), handler::addProduct)
                        .andRoute(DELETE("/{franchiseId}/branches/{branchId}/products/{productId}"),
                                handler::removeProduct)
                        .andRoute(PATCH("/{franchiseId}/branches/{branchId}/products/{productId}/stock"),
                                handler::updateProductStock)
                        .andRoute(PATCH("/{franchiseId}/branches/{branchId}/products/{productId}/name"),
                                handler::updateProductName));
    }
}
