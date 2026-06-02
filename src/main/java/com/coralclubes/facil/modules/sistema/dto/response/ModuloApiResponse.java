package com.coralclubes.facil.modules.sistema.dto.response;

import com.coralclubes.facil.shared.domain.tree.TreeNode;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa el árbol jerárquico de los módulos del sistema
 * donde se agregan campos de hijos para representar la jerarquía
 * y nivel para identificar el nivel del módulo en el árbol.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuloApiResponse implements TreeNode {
    private Long id;
    private Long idPadre;
    private String clave;
    private String nombre;
    private String ruta;
    private String icono;
    private String nivel;
    private Integer menuFacil;
    private Long orden;

    @Builder.Default
    private List<TreeNode> hijos = new ArrayList<>();

    @Override
    public Long getOrder() {
        return this.orden != null ? this.orden : 0L;
    }

    @Override
    public void setLevel(String level) { this.nivel = level; }

    @Override
    public List<TreeNode> getChildren() { return this.hijos; }

    @Override
    public void setChildren(List<TreeNode> children) {
        this.hijos = children != null ? children : new ArrayList<>();
    }
}
