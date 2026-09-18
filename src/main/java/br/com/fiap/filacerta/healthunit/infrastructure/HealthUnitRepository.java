package br.com.fiap.filacerta.healthunit.infrastructure;

import br.com.fiap.filacerta.healthunit.domain.HealthUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HealthUnitRepository extends JpaRepository<HealthUnit, UUID> {

    boolean existsByCode(String code);
}
