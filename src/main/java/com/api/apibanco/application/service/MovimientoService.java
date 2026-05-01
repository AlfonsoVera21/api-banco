package com.api.apibanco.application.service;

import com.api.apibanco.application.dto.request.MovimientoRequest;
import com.api.apibanco.application.dto.response.MovimientoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MovimientoService {

    MovimientoResponse crear(MovimientoRequest request);

    List<MovimientoResponse> listar();

    Page<MovimientoResponse> listar(String q, Pageable pageable);

    MovimientoResponse obtenerPorId(Long id);

    MovimientoResponse actualizar(Long id, MovimientoRequest request);

    void eliminar(Long id);
}
