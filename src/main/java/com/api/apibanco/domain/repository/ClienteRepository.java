package com.api.apibanco.domain.repository;

import com.api.apibanco.domain.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByEstadoTrue();

    @Query("""
            select c
            from Cliente c
            where c.estado = true
              and (
                  :q is null
                  or lower(c.nombre) like concat('%', :q, '%')
                  or lower(c.genero) like concat('%', :q, '%')
                  or lower(c.identificacion) like concat('%', :q, '%')
                  or lower(c.direccion) like concat('%', :q, '%')
                  or lower(c.telefono) like concat('%', :q, '%')
                  or str(c.edad) like concat('%', :q, '%')
                  or str(c.id) like concat('%', :q, '%')
              )
            """)
    Page<Cliente> buscarActivos(@Param("q") String q, Pageable pageable);

    Optional<Cliente> findByIdAndEstadoTrue(Long id);

    boolean existsByIdentificacion(String identificacion);

    boolean existsByIdentificacionAndIdNot(String identificacion, Long id);
}
