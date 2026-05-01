package com.api.apibanco.infrastructure.controller;

import com.api.apibanco.application.dto.request.ClienteRequest;
import com.api.apibanco.application.dto.response.ClienteResponse;
import com.api.apibanco.application.dto.response.PageResponse;
import com.api.apibanco.application.service.ClienteService;
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
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private static final int PAGE_DEFAULT = 0;
    private static final int SIZE_DEFAULT = 20;

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClienteResponse>> crear(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Cliente creado correctamente", clienteService.crear(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ClienteResponse>>> listar(
            @RequestParam(defaultValue = "") String busqueda
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Clientes consultados correctamente",
                PageResponse.from(clienteService.listar(busqueda, PageRequest.of(PAGE_DEFAULT, SIZE_DEFAULT, Sort.by("id").ascending())))
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponse>> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Cliente consultado correctamente", clienteService.obtenerPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponse>> actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cliente actualizado correctamente", clienteService.actualizar(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
