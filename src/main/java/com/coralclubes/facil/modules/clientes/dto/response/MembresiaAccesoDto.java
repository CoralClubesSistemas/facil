package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record MembresiaAccesoDto(
        Integer totalRegistros,
        String membresia,
        Integer numeroBeneficiario,
        String nombreBeneficiario,
        Integer idDesarrolloAcceso,
        String desarrolloAcceso,
        LocalDateTime fechaAcceso,
        String diaAcceso,
        String trimestre,
        Integer numeroAutorizacion,
        Integer claveConceptoAutorizacion,
        String conceptoAutorizacion,
        String fechaAutorizacion,
        String usuarioAutoriza,
        String usuarioAccesa,
        String membresiaAsociada,
        String nombreInvitado,
        Boolean esFestivo,
        String tipoAcceso,
        String tipoPromocion
) {
}
