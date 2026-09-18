package br.com.fiap.filacerta.patient.infrastructure;

import br.com.fiap.filacerta.patient.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    boolean existsBySusIdentifier(String susIdentifier);
}
