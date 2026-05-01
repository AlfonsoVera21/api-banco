package com.api.apibanco.application.mapper;

import com.api.apibanco.application.dto.request.ClienteRequest;
import com.api.apibanco.application.dto.response.ClienteResponse;
import com.api.apibanco.domain.model.Cliente;

import java.util.function.Function;

public final class ClienteMapper {

    private static final Function<ClienteRequest, Cliente> TO_ENTITY = request -> Cliente.builder()
            .contrasena(request.contrasena())
            .estado(request.estado() == null ? Boolean.TRUE : request.estado())
            .build();

    public static Cliente toEntity(ClienteRequest request) {
        Cliente cliente = TO_ENTITY.apply(request);
        copyPersonaFields(cliente, request);
        return cliente;
    }

    public static void copyPersonaFields(Cliente cliente, ClienteRequest request) {
        cliente.setNombre(request.nombre());
        cliente.setGenero(request.genero());
        cliente.setEdad(request.edad());
        cliente.setIdentificacion(request.identificacion());
        cliente.setDireccion(request.direccion());
        cliente.setTelefono(request.telefono());
    }

    public static void copyClienteFields(Cliente cliente, ClienteRequest request) {
        copyPersonaFields(cliente, request);
        cliente.setContrasena(request.contrasena());
        cliente.setEstado(request.estado() == null ? Boolean.TRUE : request.estado());
    }

    public static final Function<Cliente, ClienteResponse> TO_RESPONSE = cliente -> new ClienteResponse(
            cliente.getId(),
            cliente.getNombre(),
            cliente.getGenero(),
            cliente.getEdad(),
            cliente.getIdentificacion(),
            cliente.getDireccion(),
            cliente.getTelefono(),
            cliente.getEstado()
    );

    private ClienteMapper() {
    }
}
