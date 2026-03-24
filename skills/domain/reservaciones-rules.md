# Skill: Reglas de Negocio - Módulo Reservaciones (Coral Clubes)

## 🎯 Trigger (Cuándo usar esta skill)
Activa esta skill SOLO cuando trabajes en el módulo de reservaciones (`Resv`), asignación de habitaciones, inventario hotelero, o procesos de Check-In/Check-Out.

## 🧠 1. Inventario Flotante (Asignación Sombra)
- Una reservación a futuro NUNCA cambia el estatus físico de una habitación en la tabla `RESERVACIONES_UNIDADES`.
- El apartado es puramente lógico. La disponibilidad se calcula en tiempo real restando las reservas futuras cruzando fechas mediante la vista `vwResvOcupacionFisica`.

## 🧩 2. Tetris Lógico (Reubicación Silenciosa)
- Al hacer Check-In o Transferencia en mostrador, el sistema permite seleccionar una habitación que lógicamente ya estaba "apartada" por una reserva a futuro (estatus `PENDIENTE` o `CONFIRMADA`).
- Si esto ocurre, el Stored Procedure DEBE buscar disponibilidad para esa reserva futura afectada, reubicarla automáticamente en otra habitación del mismo tipo, y liberar la habitación deseada para el huésped en mostrador.

## 🔗 3. Jerarquía Lock-offs (Padres e Hijas)
- Las habitaciones pueden componerse de otras (Ej. Suite 105 [Padre] se divide en 105A y 105B [Hijas]).
- **Regla de Bloqueo:** Si se ocupa, ensucia o bloquea una habitación Padre, TODAS sus sub-habitaciones (Hijas) quedan inutilizables.
- Si se ocupa, ensucia o bloquea una habitación Hija, su Padre directo queda inutilizable para la venta como unidad completa.
- Siempre se debe validar y actualizar recursivamente usando `RUN_PADRE_ID`.

## 🚪 4. Cierre Transaccional (Check-Out)
- El proceso de Check-Out NO elimina registros.
- Pasos obligatorios:
    1. Cambiar el estatus lógico de la reserva a `CHECK-OUT`.
    2. Cambiar el estatus de la habitación física a `SUCIA` (ID 5). *Nota: Sincronizar este estatus con Padres/Hijas si aplica.*
    3. Generar un registro histórico en `RESERVACIONES_CLIENTES_OUTHOUSE`.