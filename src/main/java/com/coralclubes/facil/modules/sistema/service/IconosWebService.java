package com.coralclubes.facil.modules.sistema.service;

import com.coralclubes.facil.modules.sistema.repository.IconosWebRepository;
import com.coralclubes.facil.shared.domain.dto.IconoWeb;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IconosWebService {
    private final IconosWebRepository repo;

    public List<IconoWeb> getAllIconosWeb() {
        return repo.spFacilCatalogoIconosWeb();
    }
}
