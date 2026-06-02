package com.coralclubes.facil.shared.domain.tree;

import java.util.List;

/**
 * Interfaz para representar una estructura de árbol genérica enfocada en la jerarquía.
 */
public interface TreeNode {
    Long getOrder();
    List<TreeNode> getChildren();
    void setLevel(String level);
    void setChildren(List<TreeNode> children);
}
