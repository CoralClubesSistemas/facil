package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.request.BajaAccesoPreferencialRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.InsertarAccesoPreferencialRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.BeneficiarioAccesoVigenteResponse;
import com.coralclubes.facil.modules.cobranza.repository.AccesosCobranzaRepository;
import com.coralclubes.logging.BusinessLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccesosCobranzaService {

    private final AccesosCobranzaRepository repository;
    private final BusinessLogger businessLogger;

    public void insertarAccesoPreferencial(InsertarAccesoPreferencialRequest request, String usuario) {
        repository.sp_InsertarBeneficiarioAccesoPreferencial(
                request.membresia(),
                request.numBeneficiario(),
                1158,
                request.motivo(),
                request.notaRecomendaciones(),
                request.fechaInicio(),
                request.fechaFinal(),
                usuario
        );

        businessLogger.info(usuario, "Acceso preferencial registrado/actualizado para membresia: {}, beneficiario: {}",
                request.membresia(), request.numBeneficiario());
    }

    public Optional<BeneficiarioAccesoVigenteResponse> obtenerAccesoVigente(String membresia, Integer numBeneficiario) {
        return repository.sp_ObtenerBeneficiarioAccesoVigente(membresia, numBeneficiario);
    }

    public void bajaAccesoPreferencial(BajaAccesoPreferencialRequest request, String usuario) {
        repository.sp_BajaBeneficiarioAccesoPreferencial(
                request.membresia(),
                request.numBeneficiario(),
                usuario
        );

        businessLogger.info(usuario, "Baja de acceso preferencial para membresia: {}, beneficiario: {}",
                request.membresia(), request.numBeneficiario());
    }
}
