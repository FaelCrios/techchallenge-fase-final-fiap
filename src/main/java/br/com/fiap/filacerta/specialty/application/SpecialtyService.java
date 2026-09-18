package br.com.fiap.filacerta.specialty.application;

import br.com.fiap.filacerta.shared.exception.ConflictException;
import br.com.fiap.filacerta.shared.exception.NotFoundException;
import br.com.fiap.filacerta.specialty.api.CreateSpecialtyRequest;
import br.com.fiap.filacerta.specialty.api.SpecialtyResponse;
import br.com.fiap.filacerta.specialty.domain.Specialty;
import br.com.fiap.filacerta.specialty.infrastructure.SpecialtyRepository;
import org.hibernate.annotations.NotFound;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    public SpecialtyService(SpecialtyRepository specialtyRepository) {
        this.specialtyRepository = specialtyRepository;
    }


    @Transactional
    public SpecialtyResponse create(CreateSpecialtyRequest createSpecialtyRequest) {
        String normalizedCode = normalizeCode(createSpecialtyRequest.code());

        String normalizedName = createSpecialtyRequest.name().trim();

        validateCodeDoesNotExist(normalizedCode);

        Specialty specialty = new Specialty(normalizedCode, normalizedName);

        Specialty saved = specialtyRepository.save(specialty);
        return SpecialtyResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public SpecialtyResponse findById(UUID id){
        Specialty  specialty = specialtyRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Especialidade não encontrada: "+id)
        );
        return SpecialtyResponse.from(specialty);
    }

    @Transactional(readOnly = true)
    public List<SpecialtyResponse> findAll(){
       return specialtyRepository.findAll().stream().map(SpecialtyResponse::from).toList();
    }

    private void validateCodeDoesNotExist(String code){
        if(specialtyRepository.existsByCode(code)){
            throw new ConflictException("Já existe uma especialidade com o código: "+code);
        }
    }

    private String normalizeCode(String code){
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
