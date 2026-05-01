package com.api.apibanco.application.service.impl;

import com.api.apibanco.application.dto.response.ReporteEstadoCuentaResponse;
import com.api.apibanco.application.dto.response.ReporteMovimientoResponse;
import com.api.apibanco.application.service.ReporteService;
import com.api.apibanco.domain.model.Cliente;
import com.api.apibanco.domain.model.Movimiento;
import com.api.apibanco.domain.repository.ClienteRepository;
import com.api.apibanco.domain.repository.MovimientoRepository;
import com.api.apibanco.infrastructure.exception.BusinessException;
import com.api.apibanco.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private static final BigDecimal CERO = BigDecimal.ZERO;
    private static final DateTimeFormatter FECHA_REPORTE = DateTimeFormatter.ofPattern("d/M/yyyy");

    private final ClienteRepository clienteRepository;
    private final MovimientoRepository movimientoRepository;

    @Override
    public ReporteEstadoCuentaResponse generarEstadoCuenta(Long clienteId, LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio.isAfter(fechaFin)) {
            throw new BusinessException("La fechaInicio no puede ser mayor a la fechaFin");
        }
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id " + clienteId));
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.plusDays(1).atStartOfDay().minusNanos(1);

        List<ReporteMovimientoResponse> movimientos = movimientoRepository.findReporteMovimientos(clienteId, inicio, fin)
                .stream()
                .map(this::toReporteMovimiento)
                .toList();

        BigDecimal totalCreditos = movimientos.stream()
                .map(ReporteMovimientoResponse::movimiento)
                .filter(valor -> valor.compareTo(CERO) > 0)
                .reduce(CERO, BigDecimal::add);
        BigDecimal totalDebitos = movimientos.stream()
                .map(ReporteMovimientoResponse::movimiento)
                .filter(valor -> valor.compareTo(CERO) < 0)
                .map(BigDecimal::abs)
                .reduce(CERO, BigDecimal::add);

        return new ReporteEstadoCuentaResponse(
                cliente.getId(),
                cliente.getNombre(),
                fechaInicio,
                fechaFin,
                totalCreditos,
                totalDebitos,
                movimientos,
                generarPdfBase64(cliente, fechaInicio, fechaFin, movimientos, totalCreditos, totalDebitos)
        );
    }

    private ReporteMovimientoResponse toReporteMovimiento(Movimiento movimiento) {
        return new ReporteMovimientoResponse(
                movimiento.getFecha(),
                movimiento.getCuenta().getCliente().getNombre(),
                movimiento.getCuenta().getNumeroCuenta(),
                movimiento.getCuenta().getTipoCuenta(),
                movimiento.getCuenta().getSaldoInicial(),
                movimiento.getCuenta().getEstado(),
                movimiento.getValor(),
                movimiento.getSaldo()
        );
    }

    private String generarPdfBase64(
            Cliente cliente,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            List<ReporteMovimientoResponse> movimientos,
            BigDecimal totalCreditos,
            BigDecimal totalDebitos
    ) {
        return Base64.getEncoder().encodeToString(
                construirPdf(cliente, fechaInicio, fechaFin, movimientos, totalCreditos, totalDebitos)
        );
    }

    private byte[] construirPdf(
            Cliente cliente,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            List<ReporteMovimientoResponse> movimientos,
            BigDecimal totalCreditos,
            BigDecimal totalDebitos
    ) {
        StringBuilder contenido = new StringBuilder();
        escribirTexto(contenido, "Estado de Cuenta", 40, 760, 14);
        escribirTexto(contenido, "Cliente: " + cliente.getNombre(), 40, 725, 10);
        escribirTexto(contenido, "Rango: " + fechaInicio + " - " + fechaFin, 40, 708, 10);
        escribirTexto(contenido, "Total creditos: " + formatear(totalCreditos), 40, 691, 10);
        escribirTexto(contenido, "Total debitos: " + formatear(totalDebitos), 40, 674, 10);

        int tablaX = 35;
        int tablaY = 630;
        int filaAlto = 24;
        int[] anchos = {60, 105, 72, 65, 72, 48, 70, 82};
        String[] encabezados = {
                "Fecha",
                "Cliente",
                "Numero Cuenta",
                "Tipo",
                "Saldo Inicial",
                "Estado",
                "Movimiento",
                "Saldo Disp."
        };

        dibujarFila(contenido, tablaX, tablaY, filaAlto, anchos);
        escribirCeldas(contenido, encabezados, tablaX, tablaY, filaAlto, anchos, 8);

        int y = tablaY - filaAlto;
        for (ReporteMovimientoResponse movimiento : movimientos) {
            dibujarFila(contenido, tablaX, y, filaAlto, anchos);
            escribirCeldas(contenido, new String[]{
                    movimiento.fecha().format(FECHA_REPORTE),
                    movimiento.cliente(),
                    movimiento.numeroCuenta(),
                    movimiento.tipo().name(),
                    formatear(movimiento.saldoInicial()),
                    movimiento.estado().toString(),
                    formatear(movimiento.movimiento()),
                    formatear(movimiento.saldoDisponible())
            }, tablaX, y, filaAlto, anchos, 8);
            y -= filaAlto;
        }

        String stream = contenido.toString();
        String[] objetos = {
                "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n",
                "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n",
                "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n",
                "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n",
                "5 0 obj\n<< /Length " + stream.getBytes(StandardCharsets.UTF_8).length + " >>\nstream\n" + stream + "endstream\nendobj\n"
        };
        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        for (String objeto : objetos) {
            offsets.add(pdf.toString().getBytes(StandardCharsets.UTF_8).length);
            pdf.append(objeto);
        }
        int xref = pdf.toString().getBytes(StandardCharsets.UTF_8).length;
        pdf.append("xref\n0 6\n0000000000 65535 f \n");
        offsets.forEach(offset -> pdf.append(String.format("%010d 00000 n \n", offset)));
        pdf.append("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n")
                .append(xref)
                .append("\n%%EOF");
        return pdf.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void dibujarFila(StringBuilder contenido, int x, int y, int alto, int[] anchos) {
        int anchoTotal = 0;
        for (int ancho : anchos) {
            anchoTotal += ancho;
        }
        contenido.append(String.format(Locale.US, "%.2f %.2f %.2f %.2f re S\n", (double) x, (double) y, (double) anchoTotal, (double) alto));
        int posicionX = x;
        for (int ancho : anchos) {
            contenido.append(String.format(Locale.US, "%.2f %.2f m %.2f %.2f l S\n", (double) posicionX, (double) y, (double) posicionX, (double) y + alto));
            posicionX += ancho;
        }
        contenido.append(String.format(Locale.US, "%.2f %.2f m %.2f %.2f l S\n", (double) posicionX, (double) y, (double) posicionX, (double) y + alto));
    }

    private void escribirCeldas(StringBuilder contenido, String[] valores, int x, int y, int alto, int[] anchos, int fuente) {
        int posicionX = x;
        for (int i = 0; i < valores.length; i++) {
            escribirTexto(contenido, ajustarTexto(valores[i], anchos[i]), posicionX + 4, y + alto - 15, fuente);
            posicionX += anchos[i];
        }
    }

    private void escribirTexto(StringBuilder contenido, String texto, int x, int y, int fuente) {
        contenido.append("BT\n")
                .append("/F1 ").append(fuente).append(" Tf\n")
                .append(x).append(' ').append(y).append(" Td\n")
                .append('(').append(escaparPdf(texto)).append(") Tj\n")
                .append("ET\n");
    }

    private String ajustarTexto(String texto, int ancho) {
        int maximo = Math.max(6, ancho / 5);
        if (texto.length() <= maximo) {
            return texto;
        }
        return texto.substring(0, maximo - 3) + "...";
    }

    private String escaparPdf(String texto) {
        return texto.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    private String formatear(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
