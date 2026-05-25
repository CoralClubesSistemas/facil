package com.coralclubes.facil.modules.notificaciones.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SISTEMA_NOTIFICACIONES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOT_ID")
    private Long id;

    @Column(name = "NOT_REMITENTE_USERNAME", nullable = false, length = 50)
    private String remitente;

    @Column(name = "NOT_DESTINATARIO_USERNAME", nullable = false, length = 50)
    private String destinatario;

    @Column(name = "NOT_TIPO_MENSAJE", nullable = false, length = 30)
    private String tipoMensaje;

    @Column(name = "NOT_NIVEL_PRIORIDAD", nullable = false)
    private Integer nivelPrioridad;

    @Column(name = "NOT_TITULO", nullable = false, length = 150)
    private String titulo;

    @Column(name = "NOT_MENSAJE", nullable = false, length = 500)
    private String mensaje;

    @Column(name = "NOT_METADATA_JSON", columnDefinition = "NVARCHAR(MAX)")
    private String metadataJson;

    @Column(name = "NOT_FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "NOT_ESTADO", nullable = false, length = 20)
    private String estado;

    @Column(name = "NOT_FECHA_LECTURA")
    private LocalDateTime fechaLectura;

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = "NO_LEIDO"; // SIEMPRE NACEN COMO NO LEÍDAS
        }
        if (this.nivelPrioridad == null) {
            this.nivelPrioridad = 1; // SIEMPRE NACEN CON NIVEL DE PRIORIDAD BAJO, A MENOS QUE EL SISTEMA INDIQUE LO CONTRARIO
        }
    }
}