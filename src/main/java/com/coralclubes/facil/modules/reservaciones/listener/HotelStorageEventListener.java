package com.coralclubes.facil.modules.reservaciones.listener;

import com.coralclubes.facil.modules.reservaciones.service.HotelesService;
import com.coralclubes.facil.shared.infrastructure.integration.storage.event.StorageFileProcessedEvent;
import com.coralclubes.logging.BusinessLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Escucha los eventos internos de almacenamiento (StorageFileProcessedEvent)
 * y ejecuta lógica de negocio del módulo de Reservaciones/Hoteles cuando la imagen está lista.
 */
@Component
@RequiredArgsConstructor
public class HotelStorageEventListener {

    private final HotelesService hotelesService;
    private final BusinessLogger logger;

    /**
     * Captura los eventos de almacenamiento procesados y filtra aquellos que pertenecen al módulo de HOTELES.
     *
     * @param event El evento interno de archivo procesado en storage.
     */
    @EventListener
    public void handleHotelStorageEvent(StorageFileProcessedEvent event) {
        String modulo = event.getMetadataValue("modulo");

        if ("HOTELES".equalsIgnoreCase(modulo)) {
            logger.info("HOTEL_STORAGE_LISTENER", "Procesando evento de storage para hotel. UUID: {}, Estado: {}",
                    event.fileId(), event.status());

            // Solo procesamos la inserción si el archivo fue cargado exitosamente (DISPONIBLE)
            if ("DISPONIBLE".equalsIgnoreCase(event.status())) {
                try {
                    String idHotelStr = event.getMetadataValue("idHotel");
                    String subidoPor = event.getMetadataValue("subidoPor");

                    if (idHotelStr != null) {
                        Integer hotelId = Integer.parseInt(idHotelStr);
                        String usuario = subidoPor != null ? subidoPor : "SYSTEM_EVENT";

                        hotelesService.registrarImagenProcesada(hotelId, event.fileId(), usuario);

                        logger.info("HOTEL_STORAGE_LISTENER", "Imagen registrada exitosamente para el hotel ID: {}, Archivo UUID: {}",
                                hotelId, event.fileId());
                    } else {
                        logger.warn("HOTEL_STORAGE_LISTENER", "Se recibió un evento para HOTELES pero falta el metadato 'idHotel'. UUID: {}", event.fileId());
                    }
                } catch (Exception e) {
                    logger.error("HOTEL_STORAGE_LISTENER", "Error al registrar la imagen del hotel en la base de datos", e);
                }
            } else {
                logger.warn("HOTEL_STORAGE_LISTENER", "El procesamiento del archivo UUID {} finalizó con estatus no exitoso: {}. Mensaje: {}",
                        event.fileId(), event.status(), event.message());
            }
        }
    }
}
