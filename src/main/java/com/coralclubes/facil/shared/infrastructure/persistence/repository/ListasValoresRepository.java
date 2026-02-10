package com.coralclubes.facil.shared.infrastructure.persistence.repository;

import com.coralclubes.facil.shared.infrastructure.persistence.entities.ListasValores;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListasValoresRepository extends JpaRepository<ListasValores, Integer> {
    List<ListasValores> findByTabla(String tabla);
    Optional<ListasValores> findByTablaAndClave(String tabla, String clave);
    Optional<ListasValores> findByTablaAndDescripcion(String tabla, String descripcion);
}
