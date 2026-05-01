package com.api.apibanco.domain.repository;

import com.api.apibanco.domain.model.Cuenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    List<Cuenta> findByEstadoTrue();

    List<Cuenta> findByEstadoTrueAndClienteEstadoTrue();

    @Query("""
            select c
            from Cuenta c
            join c.cliente cliente
            where c.estado = true
              and cliente.estado = true
              and (
                  :q is null
                  or lower(c.numeroCuenta) like concat('%', :q, '%')
                  or lower(cast(c.tipoCuenta as string)) like concat('%', :q, '%')
                  or lower(cliente.nombre) like concat('%', :q, '%')
                  or lower(cliente.identificacion) like concat('%', :q, '%')
                  or str(c.id) like concat('%', :q, '%')
                  or str(c.saldoInicial) like concat('%', :q, '%')
              )
            """)
    Page<Cuenta> buscarActivas(@Param("q") String q, Pageable pageable);

    List<Cuenta> findByClienteIdAndEstadoTrue(Long clienteId);

    Optional<Cuenta> findByIdAndEstadoTrueAndClienteEstadoTrue(Long id);

    boolean existsByNumeroCuenta(String numeroCuenta);

    boolean existsByNumeroCuentaAndIdNot(String numeroCuenta, Long id);
}
