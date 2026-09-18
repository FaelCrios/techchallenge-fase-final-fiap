package br.com.fiap.filacerta.healthunit.application;

import br.com.fiap.filacerta.healthunit.api.CreateHealthUnitRequest;
import br.com.fiap.filacerta.healthunit.api.HealthUnitResponse;
import br.com.fiap.filacerta.healthunit.domain.HealthUnit;
import br.com.fiap.filacerta.healthunit.infrastructure.HealthUnitRepository;
import br.com.fiap.filacerta.shared.exception.ConflictException;
import br.com.fiap.filacerta.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthUnitServiceTest {

    @Mock
    private HealthUnitRepository healthUnitRepository;

    private HealthUnitService healthUnitService;

    @BeforeEach
    void setUp() {
        healthUnitService =
                new HealthUnitService(
                        healthUnitRepository
                );
    }

    @Test
    void shouldCreateHealthUnit() {

        CreateHealthUnitRequest request =
                new CreateHealthUnitRequest(
                        "ubs-001",
                        "UBS Central"
                );

        when(
                healthUnitRepository
                        .existsByCode("UBS-001")
        ).thenReturn(false);

        when(
                healthUnitRepository
                        .save(any(HealthUnit.class))
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        HealthUnitResponse response =
                healthUnitService.create(request);

        assertThat(response.code())
                .isEqualTo("UBS-001");

        assertThat(response.name())
                .isEqualTo("UBS Central");

        assertThat(response.id())
                .isNotNull();

        verify(healthUnitRepository)
                .existsByCode("UBS-001");

        verify(healthUnitRepository)
                .save(any(HealthUnit.class));
    }

    @Test
    void shouldThrowConflictWhenCodeAlreadyExists() {

        CreateHealthUnitRequest request =
                new CreateHealthUnitRequest(
                        "ubs-001",
                        "UBS Central"
                );

        when(
                healthUnitRepository
                        .existsByCode("UBS-001")
        ).thenReturn(true);

        assertThatThrownBy(() ->
                healthUnitService.create(request)
        )
                .isInstanceOf(ConflictException.class)
                .hasMessage(
                        "Já existe uma unidade de saúde com o código: UBS-001"
                );

        verify(
                healthUnitRepository,
                never()
        ).save(any(HealthUnit.class));
    }

    @Test
    void shouldFindHealthUnitById() {

        HealthUnit healthUnit =
                new HealthUnit(
                        "UBS-001",
                        "UBS Central"
                );

        when(
                healthUnitRepository
                        .findById(healthUnit.getId())
        ).thenReturn(
                Optional.of(healthUnit)
        );

        HealthUnitResponse response =
                healthUnitService.findById(
                        healthUnit.getId()
                );

        assertThat(response.id())
                .isEqualTo(healthUnit.getId());

        assertThat(response.code())
                .isEqualTo("UBS-001");

        assertThat(response.name())
                .isEqualTo("UBS Central");
    }

    @Test
    void shouldThrowNotFoundWhenHealthUnitDoesNotExist() {

        UUID id = UUID.randomUUID();

        when(
                healthUnitRepository.findById(id)
        ).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                healthUnitService.findById(id)
        )
                .isInstanceOf(NotFoundException.class)
                .hasMessage(
                        "Unidade de saúde não encontrada: "
                                + id
                );
    }

}