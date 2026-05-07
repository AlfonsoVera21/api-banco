package com.api.apibanco.infrastructure.controller;

import com.api.apibanco.application.dto.response.BusquedaGlobalResponse;
import com.api.apibanco.application.dto.response.ClienteResponse;
import com.api.apibanco.application.dto.response.CuentaResponse;
import com.api.apibanco.application.dto.response.MovimientoResponse;
import com.api.apibanco.application.dto.response.ReporteEstadoCuentaResponse;
import com.api.apibanco.application.dto.response.ReporteMovimientoResponse;
import com.api.apibanco.application.dto.response.TransferenciaResponse;
import com.api.apibanco.application.service.BusquedaService;
import com.api.apibanco.application.service.ClienteService;
import com.api.apibanco.application.service.CuentaService;
import com.api.apibanco.application.service.MovimientoService;
import com.api.apibanco.application.service.ReporteService;
import com.api.apibanco.domain.model.enums.TipoCuenta;
import com.api.apibanco.domain.model.enums.TipoMovimiento;
import com.api.apibanco.infrastructure.exception.BusinessException;
import com.api.apibanco.infrastructure.exception.GlobalExceptionHandler;
import com.api.apibanco.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        BusquedaController.class,
        ClienteController.class,
        CuentaController.class,
        MovimientoController.class,
        ReporteController.class
})
@Import(GlobalExceptionHandler.class)
class ApiEndpointControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    @MockitoBean
    private BusquedaService busquedaService;

    @MockitoBean
    private CuentaService cuentaService;

    @MockitoBean
    private MovimientoService movimientoService;

    @MockitoBean
    private ReporteService reporteService;

    @Test
    void crearCliente_debeResponder201ConApiResponse() throws Exception {
        when(clienteService.crear(any())).thenReturn(cliente());

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJsonValido()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Cliente creado correctamente"))
                .andExpect(jsonPath("$.data.clienteId").value(1))
                .andExpect(jsonPath("$.data.nombre").value("Jose Lema"));
    }

    @Test
    void crearCliente_conDatosInvalidos_debeResponder400() throws Exception {
        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "",
                                  "genero": "MASCULINO",
                                  "edad": 30,
                                  "identificacion": "ID-001",
                                  "direccion": "Otavalo sn y principal",
                                  "telefono": "098254785",
                                  "contrasena": "123456"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error de validacion"))
                .andExpect(jsonPath("$.details.nombre").value("El nombre es obligatorio"));
    }

    @Test
    void listarClientes_sinBusqueda_debeResponderPagina() throws Exception {
        when(clienteService.listar(eq(""), any())).thenReturn(new PageImpl<>(List.of(cliente())));

        mockMvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].clienteId").value(1))
                .andExpect(jsonPath("$.data.content[0].nombre").value("Jose Lema"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void listarClientes_conBusqueda_debeResponderPaginaFiltrada() throws Exception {
        when(clienteService.listar(eq("jose"), any())).thenReturn(new PageImpl<>(List.of(cliente())));

        mockMvc.perform(get("/api/v1/clientes")
                        .param("busqueda", "jose"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].clienteId").value(1))
                .andExpect(jsonPath("$.data.content[0].nombre").value("Jose Lema"));
    }

    @Test
    void obtenerClientePorId_debeResponder200() throws Exception {
        when(clienteService.obtenerPorId(1L)).thenReturn(cliente());

        mockMvc.perform(get("/api/v1/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.clienteId").value(1))
                .andExpect(jsonPath("$.data.identificacion").value("ID-001"));
    }

    @Test
    void obtenerClientePorId_inexistente_debeResponder404() throws Exception {
        when(clienteService.obtenerPorId(99L)).thenThrow(new ResourceNotFoundException("Cliente no encontrado con id 99"));

        mockMvc.perform(get("/api/v1/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente no encontrado con id 99"));
    }

    @Test
    void actualizarCliente_debeResponder200() throws Exception {
        when(clienteService.actualizar(eq(1L), any())).thenReturn(cliente());

        mockMvc.perform(put("/api/v1/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJsonValido()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cliente actualizado correctamente"))
                .andExpect(jsonPath("$.data.nombre").value("Jose Lema"));
    }

    @Test
    void actualizarCliente_conDatosInvalidos_debeResponder400() throws Exception {
        mockMvc.perform(put("/api/v1/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Jose Lema",
                                  "genero": "MASCULINO",
                                  "edad": -1,
                                  "identificacion": "ID-001",
                                  "direccion": "Otavalo sn y principal",
                                  "telefono": "098254785",
                                  "contrasena": "123456"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.edad").value("La edad no puede ser negativa"));
    }

    @Test
    void eliminarCliente_debeResponder204() throws Exception {
        mockMvc.perform(delete("/api/v1/clientes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarCliente_inexistente_debeResponder404() throws Exception {
        doThrow(new ResourceNotFoundException("Cliente no encontrado con id 99"))
                .when(clienteService).eliminar(99L);

        mockMvc.perform(delete("/api/v1/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente no encontrado con id 99"));
    }

    @Test
    void crearCuenta_debeResponder201ConApiResponse() throws Exception {
        when(cuentaService.crear(any())).thenReturn(cuenta());

        mockMvc.perform(post("/api/v1/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuentaJsonValida()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Cuenta creada correctamente"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.numeroCuenta").value("478758"));
    }

    @Test
    void crearCuenta_conDatosInvalidos_debeResponder400() throws Exception {
        mockMvc.perform(post("/api/v1/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "numeroCuenta": "",
                                  "tipoCuenta": "AHORROS",
                                  "saldoInicial": 2000,
                                  "estado": true,
                                  "clienteId": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.numeroCuenta").value("El numero de cuenta es obligatorio"));
    }

    @Test
    void listarCuentas_sinBusqueda_debeResponderPagina() throws Exception {
        when(cuentaService.listar(eq(""), any())).thenReturn(new PageImpl<>(List.of(cuenta())));

        mockMvc.perform(get("/api/v1/cuentas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].numeroCuenta").value("478758"));
    }

    @Test
    void listarCuentas_conBusqueda_debeResponderPaginaFiltrada() throws Exception {
        when(cuentaService.listar(eq("478758"), any())).thenReturn(new PageImpl<>(List.of(cuenta())));

        mockMvc.perform(get("/api/v1/cuentas")
                        .param("busqueda", "478758"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].numeroCuenta").value("478758"));
    }

    @Test
    void obtenerCuentaPorId_debeResponder200() throws Exception {
        when(cuentaService.obtenerPorId(1L)).thenReturn(cuenta());

        mockMvc.perform(get("/api/v1/cuentas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.clienteId").value(1));
    }

    @Test
    void obtenerCuentaPorId_inexistente_debeResponder404() throws Exception {
        when(cuentaService.obtenerPorId(99L)).thenThrow(new ResourceNotFoundException("Cuenta no encontrada con id 99"));

        mockMvc.perform(get("/api/v1/cuentas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cuenta no encontrada con id 99"));
    }

    @Test
    void actualizarCuenta_debeResponder200() throws Exception {
        when(cuentaService.actualizar(eq(1L), any())).thenReturn(cuenta());

        mockMvc.perform(put("/api/v1/cuentas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuentaJsonValida()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cuenta actualizada correctamente"))
                .andExpect(jsonPath("$.data.numeroCuenta").value("478758"));
    }

    @Test
    void actualizarCuenta_conDatosInvalidos_debeResponder400() throws Exception {
        mockMvc.perform(put("/api/v1/cuentas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "numeroCuenta": "478758",
                                  "tipoCuenta": "AHORROS",
                                  "saldoInicial": -1,
                                  "estado": true,
                                  "clienteId": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.saldoInicial").value("El saldo inicial no puede ser negativo"));
    }

    @Test
    void eliminarCuenta_debeResponder204() throws Exception {
        mockMvc.perform(delete("/api/v1/cuentas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarCuenta_inexistente_debeResponder404() throws Exception {
        doThrow(new ResourceNotFoundException("Cuenta no encontrada con id 99"))
                .when(cuentaService).eliminar(99L);

        mockMvc.perform(delete("/api/v1/cuentas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cuenta no encontrada con id 99"));
    }

    @Test
    void crearMovimiento_debeResponder201ConApiResponse() throws Exception {
        when(movimientoService.crear(any())).thenReturn(movimiento());

        mockMvc.perform(post("/api/v1/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movimientoJsonValido()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Movimiento creado correctamente"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.tipoMovimiento").value("DEPOSITO"));
    }

    @Test
    void crearMovimiento_sinSaldoDisponible_debeResponder400() throws Exception {
        when(movimientoService.crear(any())).thenThrow(new BusinessException("Saldo no disponible"));

        mockMvc.perform(post("/api/v1/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fecha": "2026-04-30T06:44:53.255",
                                  "tipoMovimiento": "RETIRO",
                                  "valor": 575,
                                  "cuentaId": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Saldo no disponible"));
    }

    @Test
    void crearTransferencia_debeResponder201ConApiResponse() throws Exception {
        when(movimientoService.transferir(any())).thenReturn(transferencia());

        mockMvc.perform(post("/api/v1/movimientos/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferenciaJsonValida()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Transferencia creada correctamente"))
                .andExpect(jsonPath("$.data.movimientoOrigen.tipoMovimiento").value("RETIRO"))
                .andExpect(jsonPath("$.data.movimientoOrigen.valor").value(-150))
                .andExpect(jsonPath("$.data.movimientoDestino.tipoMovimiento").value("DEPOSITO"))
                .andExpect(jsonPath("$.data.movimientoDestino.valor").value(150));
    }

    @Test
    void crearTransferencia_conDatosInvalidos_debeResponder400() throws Exception {
        mockMvc.perform(post("/api/v1/movimientos/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fecha": "2026-04-30T06:44:53.255",
                                  "cuentaOrigenId": 1,
                                  "cuentaDestinoId": 2,
                                  "valor": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.valor").value("El valor de la transferencia debe ser mayor a cero"));
    }

    @Test
    void listarMovimientos_sinBusqueda_debeResponderPagina() throws Exception {
        when(movimientoService.listar(eq(""), any())).thenReturn(new PageImpl<>(List.of(movimiento())));

        mockMvc.perform(get("/api/v1/movimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].tipoMovimiento").value("DEPOSITO"));
    }

    @Test
    void listarMovimientos_conBusqueda_debeResponderPaginaFiltrada() throws Exception {
        when(movimientoService.listar(eq("deposito"), any())).thenReturn(new PageImpl<>(List.of(movimiento())));

        mockMvc.perform(get("/api/v1/movimientos")
                        .param("busqueda", "deposito"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].tipoMovimiento").value("DEPOSITO"));
    }

    @Test
    void obtenerMovimientoPorId_debeResponder200() throws Exception {
        when(movimientoService.obtenerPorId(1L)).thenReturn(movimiento());

        mockMvc.perform(get("/api/v1/movimientos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.cuentaId").value(1));
    }

    @Test
    void obtenerMovimientoPorId_inexistente_debeResponder404() throws Exception {
        when(movimientoService.obtenerPorId(99L)).thenThrow(new ResourceNotFoundException("Movimiento no encontrado con id 99"));

        mockMvc.perform(get("/api/v1/movimientos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Movimiento no encontrado con id 99"));
    }

    @Test
    void actualizarMovimiento_debeResponder200() throws Exception {
        when(movimientoService.actualizar(eq(1L), any())).thenReturn(movimiento());

        mockMvc.perform(put("/api/v1/movimientos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movimientoJsonValido()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Movimiento actualizado correctamente"))
                .andExpect(jsonPath("$.data.tipoMovimiento").value("DEPOSITO"));
    }

    @Test
    void actualizarMovimiento_conDatosInvalidos_debeResponder400() throws Exception {
        mockMvc.perform(put("/api/v1/movimientos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fecha": "2026-04-30T06:44:53.255",
                                  "valor": 100,
                                  "cuentaId": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.tipoMovimiento").value("El tipo de movimiento es obligatorio"));
    }

    @Test
    void eliminarMovimiento_debeResponder204() throws Exception {
        mockMvc.perform(delete("/api/v1/movimientos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarMovimiento_inexistente_debeResponder404() throws Exception {
        doThrow(new ResourceNotFoundException("Movimiento no encontrado con id 99"))
                .when(movimientoService).eliminar(99L);

        mockMvc.perform(delete("/api/v1/movimientos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Movimiento no encontrado con id 99"));
    }

    @Test
    void generarReporte_debeResponder200ConMovimientosYPdfBase64() throws Exception {
        when(reporteService.generarEstadoCuenta(
                eq(2L),
                eq(LocalDate.of(2026, 4, 1)),
                eq(LocalDate.of(2026, 4, 30))
        )).thenReturn(reporte());

        mockMvc.perform(get("/api/v1/reportes")
                        .param("clienteId", "2")
                        .param("fechaInicio", "2026-04-01")
                        .param("fechaFin", "2026-04-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data['Cliente']").value("Marianela Montalvo"))
                .andExpect(jsonPath("$.data['Movimientos'][0]['Numero Cuenta']").value("225487"))
                .andExpect(jsonPath("$.data['Movimientos'][0]['Saldo Disponible']").value(700))
                .andExpect(jsonPath("$.data['Reporte PDF Base64']").value("JVBERi0xLjQ="));
    }

    @Test
    void generarReporte_sinParametroRequerido_debeResponder400() throws Exception {
        mockMvc.perform(get("/api/v1/reportes")
                        .param("fechaInicio", "2026-04-01")
                        .param("fechaFin", "2026-04-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Falta un parametro requerido"))
                .andExpect(jsonPath("$.details.parameter").value("clienteId"));
    }

    @Test
    void buscarGlobal_conBusqueda_debeResponderResultadosAgrupados() throws Exception {
        when(busquedaService.buscar("jose")).thenReturn(busquedaGlobal("jose", List.of(cliente())));

        mockMvc.perform(get("/api/v1/busqueda")
                        .param("busqueda", "jose"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.termino").value("jose"))
                .andExpect(jsonPath("$.data.totalClientes").value(1))
                .andExpect(jsonPath("$.data.clientes[0].nombre").value("Jose Lema"))
                .andExpect(jsonPath("$.data.cuentas[0].numeroCuenta").value("478758"))
                .andExpect(jsonPath("$.data.movimientos[0].tipoMovimiento").value("DEPOSITO"));
    }

    @Test
    void buscarGlobal_sinBusqueda_debeResponderResultadosAgrupados() throws Exception {
        when(busquedaService.buscar("")).thenReturn(busquedaGlobal(null, List.of(cliente())));

        mockMvc.perform(get("/api/v1/busqueda"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.totalClientes").value(1))
                .andExpect(jsonPath("$.data.clientes[0].clienteId").value(1));
    }

    private ClienteResponse cliente() {
        return new ClienteResponse(
                1L,
                "Jose Lema",
                "MASCULINO",
                30,
                "ID-001",
                "Otavalo sn y principal",
                "098254785",
                true
        );
    }

    private CuentaResponse cuenta() {
        return new CuentaResponse(
                1L,
                "478758",
                TipoCuenta.AHORROS,
                BigDecimal.valueOf(2000),
                true,
                1L
        );
    }

    private MovimientoResponse movimiento() {
        return new MovimientoResponse(
                1L,
                LocalDateTime.of(2026, 4, 30, 6, 44, 53),
                TipoMovimiento.DEPOSITO,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(2100),
                true,
                1L
        );
    }

    private TransferenciaResponse transferencia() {
        MovimientoResponse movimientoOrigen = new MovimientoResponse(
                10L,
                LocalDateTime.of(2026, 4, 30, 6, 44, 53),
                TipoMovimiento.RETIRO,
                BigDecimal.valueOf(-150),
                BigDecimal.valueOf(1850),
                true,
                1L
        );
        MovimientoResponse movimientoDestino = new MovimientoResponse(
                11L,
                LocalDateTime.of(2026, 4, 30, 6, 44, 53),
                TipoMovimiento.DEPOSITO,
                BigDecimal.valueOf(150),
                BigDecimal.valueOf(250),
                true,
                2L
        );
        return new TransferenciaResponse(movimientoOrigen, movimientoDestino);
    }

    private ReporteEstadoCuentaResponse reporte() {
        ReporteMovimientoResponse movimiento = new ReporteMovimientoResponse(
                LocalDateTime.of(2026, 4, 30, 13, 41, 5),
                "Marianela Montalvo",
                "225487",
                TipoCuenta.CORRIENTE,
                BigDecimal.valueOf(100),
                true,
                BigDecimal.valueOf(600),
                BigDecimal.valueOf(700)
        );
        return new ReporteEstadoCuentaResponse(
                2L,
                "Marianela Montalvo",
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                BigDecimal.valueOf(600),
                BigDecimal.ZERO,
                List.of(movimiento),
                "JVBERi0xLjQ="
        );
    }

    private BusquedaGlobalResponse busquedaGlobal(String termino, List<ClienteResponse> clientes) {
        return new BusquedaGlobalResponse(
                termino,
                clientes.size(),
                1,
                1,
                clientes,
                List.of(cuenta()),
                List.of(movimiento())
        );
    }

    private String clienteJsonValido() {
        return """
                {
                  "nombre": "Jose Lema",
                  "genero": "MASCULINO",
                  "edad": 30,
                  "identificacion": "ID-001",
                  "direccion": "Otavalo sn y principal",
                  "telefono": "098254785",
                  "contrasena": "123456",
                  "estado": true
                }
                """;
    }

    private String cuentaJsonValida() {
        return """
                {
                  "numeroCuenta": "478758",
                  "tipoCuenta": "AHORROS",
                  "saldoInicial": 2000,
                  "estado": true,
                  "clienteId": 1
                }
                """;
    }

    private String movimientoJsonValido() {
        return """
                {
                  "fecha": "2026-04-30T06:44:53.255",
                  "tipoMovimiento": "DEPOSITO",
                  "valor": 100,
                  "cuentaId": 1
                }
                """;
    }

    private String transferenciaJsonValida() {
        return """
                {
                  "fecha": "2026-04-30T06:44:53.255",
                  "cuentaOrigenId": 1,
                  "cuentaDestinoId": 2,
                  "valor": 150
                }
                """;
    }
}
