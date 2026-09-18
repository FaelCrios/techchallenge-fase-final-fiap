package br.com.fiap.filacerta.healthunit.application;

import br.com.fiap.filacerta.healthunit.api.CreateHealthUnitRequest;
import br.com.fiap.filacerta.healthunit.api.HealthUnitResponse;
import br.com.fiap.filacerta.healthunit.domain.HealthUnit;
import br.com.fiap.filacerta.healthunit.infrastructure.HealthUnitRepository;
import br.com.fiap.filacerta.shared.exception.ConflictException;
import br.com.fiap.filacerta.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class HealthUnitService {

    private final HealthUnitRepository healthUnitRepository;

    public HealthUnitService(
            HealthUnitRepository healthUnitRepository
    ) {
        this.healthUnitRepository = healthUnitRepository;
    }

    @Transactional
    public HealthUnitResponse create(
            CreateHealthUnitRequest request
    ) {

        String normalizedCode = normalizeCode(request.code());
        String normalizedName = request.name().trim();

        validateCodeDoesNotExist(normalizedCode);

        HealthUnit healthUnit = new HealthUnit(
                normalizedCode,
                normalizedName
        );

        HealthUnit savedHealthUnit =
                healthUnitRepository.save(healthUnit);

        return HealthUnitResponse.from(savedHealthUnit);
    }

    @Transactional(readOnly = true)
    public HealthUnitResponse  findById(UUID id){
        HealthUnit healthUnit = healthUnitRepository.findById(id).orElseThrow(() ->new NotFoundException("Unidade de saúde não encontrada: " + id));
        return HealthUnitResponse.from(healthUnit);
    }

    @Transactional(readOnly = true)
    public List<HealthUnitResponse> findAll(){
        return healthUnitRepository.findAll().stream().map(HealthUnitResponse::from).toList();
    }

    private void validateCodeDoesNotExist(String code) {
        if (healthUnitRepository.existsByCode(code)) {
            throw new ConflictException(
                    "Já existe uma unidade de saúde com o código: " + code
            );
        }
    }

    private String normalizeCode(String code) {
        return code
                .trim()
                .toUpperCase(Locale.ROOT);
    }
}