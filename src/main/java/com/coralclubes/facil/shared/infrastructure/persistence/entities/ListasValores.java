package com.coralclubes.facil.shared.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "LISTAS_VALORES")
public class ListasValores {
    @Id
    @Column(name = "LSV_ID")
    private Integer id;

    @Column(name = "LSV_DESCRIPCION", length = 75, nullable = false)
    private String descripcion;

    @Column(name = "LSV_CLAVE", length = 50, nullable = false)
    private String clave;

    @Column(name = "LSV_TABLA", length = 50, nullable = false)
    private String tabla;

    @Column(name = "LSV_FECHA_REGISTRO", nullable = false)
    private String fechaRegistro;
}
