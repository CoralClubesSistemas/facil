package com.coralclubes.facil.modules.clientes.service;

import com.coralclubes.facil.modules.clientes.dto.request.ActualizarDatosTelefonoRequest;
import com.coralclubes.facil.modules.clientes.dto.request.TelefonoPrioridadDto;
import com.coralclubes.facil.modules.clientes.dto.response.MembresiaLlamadaResponse;
import com.coralclubes.facil.modules.clientes.dto.response.MembresiaTelefonoResponse;
import com.coralclubes.facil.modules.clientes.repository.TelefonosRepository;
import com.coralclubes.facil.modules.usuarios.service.UserContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TelefonosService {

    private final TelefonosRepository repository;
    private final UserContext userContext;
    private final ObjectMapper objectMapper;

    public List<MembresiaTelefonoResponse> obtenerNumerosTelefonos(String membresia) {
        return repository.spMembresiaObtenerNumerosTelefonos(membresia);
    }

    public void actualizarEstatusTelefono(String membresia, String numeroTelefono, Boolean estatus) {
        repository.spMembresiaActualizarEstatusTelefono(membresia, numeroTelefono, estatus);
    }

    public void actualizarDatosTelefono(String membresia, String numeroTelefono, ActualizarDatosTelefonoRequest request) {
        String usuario = userContext.getUsername();
        repository.spMembresiaActualizarDatosTelefono(
                membresia,
                numeroTelefono,
                request.nuevoNumeroTelefono(),
                request.lada(),
                request.extensionPrincipal(),
                request.extensionAlterna(),
                request.tipoTelefono(),
                request.observaciones(),
                usuario
        );
    }

    public void reordenarPrioridadTelefonos(String membresia, List<TelefonoPrioridadDto> reordenamiento) {
        String usuario = userContext.getUsername();
        try {
            String jsonReordenamiento = objectMapper.writeValueAsString(reordenamiento);
            repository.spMembresiaReordenarPrioridadTelefonos(membresia, jsonReordenamiento, usuario);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error al serializar el reordenamiento de prioridades", e);
        }
    }

    public List<MembresiaLlamadaResponse> obtenerBitacoraLlamadas(String membresia) {
        return repository.spMembresiaObtenerBitacoraLlamadas(membresia);
    }
}
