package com.coralclubes.facil.shared.utils;

import com.coralclubes.facil.shared.domain.tree.TreeNode;

import java.util.*;
import java.util.function.Function;

public class TreeGenerator {

    /**
     * Construye un árbol jerárquico a partir de una lista plana.
     * Se asume de manera predeterminada que los nodos raíz tienen rootFatherId = null.
     *
     * @param flatList La lista plana de elementos a convertir en árbol.
     * @param mapper Función para mapear cada elemento plano a un nodo de árbol.
     * @param idExtractor Función para extraer el ID único de cada elemento plano.
     * @param fatherIdExtractor Función para extraer el ID del padre de cada elemento plano.
     * @param <B> Tipo de los elementos en la lista plana.
     * @param <R> Tipo de los nodos del árbol, que debe extender TreeNode.
     */
    public static <B, R extends TreeNode> List<R> generateTree(
            List<B> flatList,
            Function<B, R> mapper,
            Function<B, Long> idExtractor,
            Function<B, Long> fatherIdExtractor) {
        return generateTree(flatList, mapper, idExtractor, fatherIdExtractor, null);
    }

    /**
     * Construye un árbol jerárquico permitiendo especificar el ID del padre raíz (ej. 10L).
     *
     * @param flatList La lista plana de elementos a convertir en árbol.
     * @param mapper Función para mapear cada elemento plano a un nodo de árbol.
     * @param idExtractor Función para extraer el ID único de cada elemento plano.
     * @param fatherIdExtractor Función para extraer el ID del padre de cada elemento plano.
     * @param rootFatherId El ID del padre que identifica a los nodos raíz (puede ser null para indicar que los nodos raíz tienen fatherId null).
     * @param <B> Tipo de los elementos en la lista plana.
     * @param <R> Tipo de los nodos del árbol, que debe extender TreeNode.
     */
    @SuppressWarnings("unchecked")
    public static <B, R extends TreeNode> List<R> generateTree(
            List<B> flatList,
            Function<B, R> mapper,
            Function<B, Long> idExtractor,
            Function<B, Long> fatherIdExtractor,
            Long rootFatherId) {

        if (flatList == null || flatList.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. Mapear elementos planos a elementos de árbol y agruparlos en un mapa indexado por ID
        Map<Long, R> nodeMap = new LinkedHashMap<>();
        flatList.forEach(item -> nodeMap.put(idExtractor.apply(item), mapper.apply(item)));

        List<R> rootNodes = new ArrayList<>();

        // 2. Construir la estructura jerárquica
        flatList.forEach(item -> {
            Long id = idExtractor.apply(item);
            Long fatherId = fatherIdExtractor.apply(item);
            R node = nodeMap.get(id);

            if (isRoot(fatherId, rootFatherId)) {
                rootNodes.add(node);
            } else if (nodeMap.containsKey(fatherId)) {
                R parent = nodeMap.get(fatherId);
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(node);
            }
        });

        // 3. Asignar niveles y ordenar recursivamente
        rootNodes.forEach(root -> processNodeRecursively(root, 0));
        rootNodes.sort(Comparator.comparing(TreeNode::getOrder, Comparator.nullsLast(Long::compareTo)));

        return rootNodes;
    }

    private static boolean isRoot(Long fatherId, Long rootFatherId) {
        if (rootFatherId == null) {
            return fatherId == null;
        }
        return rootFatherId.equals(fatherId);
    }

    @SuppressWarnings("unchecked")
    private static <R extends TreeNode> void processNodeRecursively(R node, int depth) {
        node.setLevel("LEVEL_" + depth);

        List<TreeNode> children = node.getChildren();
        if (children != null && !children.isEmpty()) {
            // Ordenar los hijos en este nivel antes de procesar su descendencia
            children.sort(Comparator.comparing(TreeNode::getOrder, Comparator.nullsLast(Long::compareTo)));

            // Procesamiento recursivo para cada hijo
            children.forEach(child -> processNodeRecursively((R) child, depth + 1));
        }
    }
}