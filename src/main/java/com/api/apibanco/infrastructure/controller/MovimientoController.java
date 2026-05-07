package com.api.apibanco.infrastructure.controller;

import com.api.apibanco.application.dto.request.MovimientoRequest;
import com.api.apibanco.application.dto.request.TransferenciaRequest;
import com.api.apibanco.application.dto.response.MovimientoResponse;
import com.api.apibanco.application.dto.response.PageResponse;
import com.api.apibanco.application.dto.response.TransferenciaResponse;
import com.api.apibanco.application.service.MovimientoService;
import com.api.apibanco.infrastructure.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private static final int PAGE_DEFAULT = 0;
    private static final int SIZE_DEFAULT = 20;

    private final MovimientoService movimientoService;

    @PostMapping
    public ResponseEntity<ApiResponse<MovimientoResponse>> crear(@Valid @RequestBody MovimientoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Movimiento creado correctamente", movimientoService.crear(request)));
    }

    @PostMapping("/transferencias")
    public ResponseEntity<ApiResponse<TransferenciaResponse>> transferir(@Valid @RequestBody TransferenciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Transferencia creada correctamente", movimientoService.transferir(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MovimientoResponse>>> listar(
            @RequestParam(defaultValue = "") String busqueda
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Movimientos consultados correctamente",
                PageResponse.from(movimientoService.listar(busqueda, PageRequest.of(PAGE_DEFAULT, SIZE_DEFAULT, Sort.by("id").ascending())))
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovimientoResponse>> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Movimiento consultado correctamente", movimientoService.obtenerPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MovimientoResponse>> actualizar(@PathVariable Long id, @Valid @RequestBody MovimientoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Movimiento actualizado correctamente", movimientoService.actualizar(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        movimientoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
