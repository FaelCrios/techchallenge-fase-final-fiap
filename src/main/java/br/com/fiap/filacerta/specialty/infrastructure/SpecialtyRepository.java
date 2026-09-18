package br.com.fiap.filacerta.specialty.infrastructure;

import br.com.fiap.filacerta.specialty.domain.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpecialtyRepository extends JpaRepository<Specialty, UUID> {

    boolean existsByCode(String code);
}
