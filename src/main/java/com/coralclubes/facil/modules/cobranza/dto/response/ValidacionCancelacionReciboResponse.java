package com.coralclubes.facil.modules.cobranza.dto.response;

import java.util.List;

public record ValidacionCancelacionReciboResponse(
        Boolean permiteCancelacion,
        List<String> bloqueos,      // Ej: "La reservación está en CHECK-OUT." (Deshabilita el botón Cancelar)
        List<String> advertencias,  // Ej: "Se cancelará el recibo, pero la reserva se mantiene por estar en CHECK-IN."
        List<AccionRequeridaDto> accionesRequeridas
) {}
