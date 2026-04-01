package com.coralclubes.facil.modules.sistema.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa el arbol gerarquico de los modulos del sistema
 * donde se agregan campos de hijos para representar la jerarquia
 * y nivel para identificar el nivel del modulo en el arbol
 * */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ModuloApiResponse {
    private Integer id;
    private Integer idPadre;
    private String clave;
    private String nombre;
    private String ruta;
    private String icono;
    private String nivel;
    private Integer menuFacil;

    @Builder.Default
    private List<ModuloApiResponse> hijos = new ArrayList<>();
}
