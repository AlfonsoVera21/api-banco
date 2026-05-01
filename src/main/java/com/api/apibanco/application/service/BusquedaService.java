package com.api.apibanco.application.service;

import com.api.apibanco.application.dto.response.BusquedaGlobalResponse;

public interface BusquedaService {

    BusquedaGlobalResponse buscar(String q);
}
