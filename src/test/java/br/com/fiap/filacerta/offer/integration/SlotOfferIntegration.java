package br.com.fiap.filacerta.offer.integration;

import br.com.fiap.filacerta.config.MutableClockTestConfig;
import br.com.fiap.filacerta.config.PostgresTestContainerConfig;
import br.com.fiap.filacerta.healthunit.domain.HealthUnit;
import br.com.fiap.filacerta.healthunit.infrastructure.HealthUnitRepository;
import br.com.fiap.filacerta.offer.api.SlotOfferResponse;
import br.com.fiap.filacerta.offer.application.SlotOfferExpirationService;
import br.com.fiap.filacerta.offer.application.SlotOfferService;
import br.com.fiap.filacerta.offer.domain.SlotOffer;
import br.com.fiap.filacerta.offer.domain.SlotOfferStatus;
import br.com.fiap.filacerta.offer.infrastructure.SlotOfferRepository;
import br.com.fiap.filacerta.patient.domain.Patient;
import br.com.fiap.filacerta.patient.infrastructure.PatientRepository;
import br.com.fiap.filacerta.scheduling.domain.AppointmentSlot;
import br.com.fiap.filacerta.scheduling.domain.AppointmentSlotStatus;
import br.com.fiap.filacerta.scheduling.infrastructure.AppointmentSlotRepository;
import br.com.fiap.filacerta.shared.exception.BusinessException;
import br.com.fiap.filacerta.specialty.domain.Specialty;
import br.com.fiap.filacerta.specialty.infrastructure.SpecialtyRepository;
import br.com.fiap.filacerta.support.MutableClock;
import br.com.fiap.filacerta.support.SlotLockTestHelper;
import br.com.fiap.filacerta.waitlist.domain.ClinicalPriority;
import br.com.fiap.filacerta.waitlist.domain.PreferredPeriod;
import br.com.fiap.filacerta.waitlist.domain.WaitlistEntry;
import br.com.fiap.filacerta.waitlist.domain.WaitlistStatus;
import br.com.fiap.filacerta.waitlist.infrastructure.WaitlistEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "filacerta.offer.expiration.enabled=false"
})
@Import({
        PostgresTestContainerConfig.class,
        MutableClockTestConfig.class,
        SlotLockTestHelper.class
})
class SlotOfferIntegrationTest {

    @Autowired
    private SlotOfferService slotOfferService;

    @Autowired
    private SlotOfferRepository slotOfferRepository;

    @Autowired
    private AppointmentSlotRepository appointmentSlotRepository;

    @Autowired
    private WaitlistEntryRepository waitlistEntryRepository;

    @Autowired
    private HealthUnitRepository healthUnitRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private SlotOfferExpirationService expirationService;

    @Autowired
    private MutableClock mutableClock;

    @Autowired
    private SlotLockTestHelper slotLockTestHelper;

    private HealthUnit healthUnit;

    private Specialty specialty;

    private Patient maria;

    private Patient joao;

    private WaitlistEntry mariaEntry;

    private WaitlistEntry joaoEntry;

    private AppointmentSlot slot;


    @BeforeEach
    void setUp() {
        healthUnit = healthUnitRepository.save(
                new HealthUnit(
                        randomCode("UBS-"),
                        "UBS Teste de Integração"
                )
        );

        specialty = specialtyRepository.save(
                new Specialty(
                        randomCode("CARD-"),
                        "Cardiologia"
                )
        );

        maria = patientRepository.save(
                new Patient(
                        randomCode("SUS-"),
                        "Maria Teste"
                )
        );

        joao = patientRepository.save(
                new Patient(
                        randomCode("SUS-"),
                        "João Teste"
                )
        );

        mariaEntry = waitlistEntryRepository.save(
                new WaitlistEntry(
                        maria,
                        healthUnit,
                        specialty,
                        ClinicalPriority.VERY_HIGH,
                        PreferredPeriod.ANY
                )
        );

        joaoEntry = waitlistEntryRepository.save(
                new WaitlistEntry(
                        joao,
                        healthUnit,
                        specialty,
                        ClinicalPriority.HIGH,
                        PreferredPeriod.ANY
                )
        );

        slot = appointmentSlotRepository.save(
                new AppointmentSlot(
                        healthUnit,
                        specialty,
                        OffsetDateTime.now(ZoneOffset.UTC)
                                .plusDays(7)
                )
        );

        mutableClock.setInstant(Instant.now());
    }

    @Test
    @Transactional
    void shouldCreateOfferForHighestPriorityPatient() {
        SlotOfferResponse response = slotOfferService.createOffer(slot.getId());

        assertNotNull(response);

        assertNotNull(response.token());

        assertEquals(
                SlotOfferStatus.PENDING,
                response.status()
        );

        assertEquals(
                maria.getId(),
                response.patientId()
        );

        assertEquals(
                mariaEntry.getId(),
                response.waitlistEntryId()
        );

        SlotOffer persistedOffer =
                slotOfferRepository
                        .findByToken(response.token())
                        .orElseThrow();

        assertEquals(
                SlotOfferStatus.PENDING,
                persistedOffer.getStatus()
        );

        assertEquals(
                slot.getId(),
                persistedOffer
                        .getAppointmentSlot()
                        .getId()
        );

        AppointmentSlot updatedSlot =
                appointmentSlotRepository
                        .findById(slot.getId())
                        .orElseThrow();

        assertEquals(
                AppointmentSlotStatus.OFFERED,
                updatedSlot.getStatus()
        );

        WaitlistEntry updatedMariaEntry =
                waitlistEntryRepository
                        .findById(mariaEntry.getId())
                        .orElseThrow();

        assertEquals(
                WaitlistStatus.OFFERED,
                updatedMariaEntry.getStatus()
        );

        WaitlistEntry updatedJoaoEntry =
                waitlistEntryRepository
                        .findById(joaoEntry.getId())
                        .orElseThrow();

        assertEquals(
                WaitlistStatus.WAITING,
                updatedJoaoEntry.getStatus()
        );
    }


    @Test
    void shouldExpireOfferAndCreateNewOfferForNextPatient() {
        SlotOfferResponse mariaResponse =
                slotOfferService.createOffer(
                        slot.getId()
                );

        assertEquals(
                maria.getId(),
                mariaResponse.patientId()
        );

        UUID mariaOfferId =
                slotOfferRepository
                        .findByToken(mariaResponse.token())
                        .orElseThrow()
                        .getId();


        Instant expirationTime =
                mariaResponse.expiresAt()
                        .toInstant()
                        .plus(1, ChronoUnit.MINUTES);

        mutableClock.setInstant(expirationTime);

        Optional<UUID> releasedSlotId =
                expirationService.expireOffer(
                        mariaOfferId
                );

        assertEquals(
                Optional.of(slot.getId()),
                releasedSlotId
        );


        SlotOffer expiredOffer =
                slotOfferRepository
                        .findById(mariaOfferId)
                        .orElseThrow();

        assertEquals(
                SlotOfferStatus.EXPIRED,
                expiredOffer.getStatus()
        );

        WaitlistEntry updatedMariaEntry =
                waitlistEntryRepository
                        .findById(mariaEntry.getId())
                        .orElseThrow();

        assertEquals(
                WaitlistStatus.WAITING,
                updatedMariaEntry.getStatus()
        );

        AppointmentSlot releasedSlot =
                appointmentSlotRepository
                        .findById(slot.getId())
                        .orElseThrow();

        assertEquals(
                AppointmentSlotStatus.AVAILABLE,
                releasedSlot.getStatus()
        );

        SlotOfferResponse joaoResponse =
                slotOfferService.createOffer(
                        slot.getId()
                );

        assertEquals(
                joao.getId(),
                joaoResponse.patientId()
        );

        assertEquals(
                SlotOfferStatus.PENDING,
                joaoResponse.status()
        );

        assertNotEquals(
                mariaResponse.token(),
                joaoResponse.token()
        );

        AppointmentSlot finalSlot =
                appointmentSlotRepository
                        .findById(slot.getId())
                        .orElseThrow();

        assertEquals(
                AppointmentSlotStatus.OFFERED,
                finalSlot.getStatus()
        );

        WaitlistEntry finalMariaEntry =
                waitlistEntryRepository
                        .findById(mariaEntry.getId())
                        .orElseThrow();

        assertEquals(
                WaitlistStatus.WAITING,
                finalMariaEntry.getStatus()
        );

        WaitlistEntry finalJoaoEntry =
                waitlistEntryRepository
                        .findById(joaoEntry.getId())
                        .orElseThrow();

        assertEquals(
                WaitlistStatus.OFFERED,
                finalJoaoEntry.getStatus()
        );
    }


    @Test
    void shouldKeepConsistentStateWhenAcceptAndExpireRunConcurrently() throws Exception {
        SlotOfferResponse response = slotOfferService.createOffer(slot.getId());

        UUID token = response.token();

        UUID offerId = slotOfferRepository
                .findByToken(token)
                .orElseThrow()
                .getId();

        Instant beforeExpiration = response.expiresAt()
                .toInstant()
                .minus(1, ChronoUnit.SECONDS);

        mutableClock.setInstant(beforeExpiration);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch startLatch = new CountDownLatch(1);

        try {
            Callable<Boolean> acceptTask = () -> {

                startLatch.await();
                try {
                    slotOfferService.acceptOffer(token);
                    return true;

                } catch (BusinessException exception) {
                    return false;
                }
            };

            Callable<Boolean> expireTask = () -> {

                startLatch.await();
                Optional<UUID> result = expirationService.expireOffer(offerId);
                return result.isPresent();
            };

            Future<Boolean> acceptFuture = executor.submit(acceptTask);
            Future<Boolean> expireFuture = executor.submit(expireTask);

            startLatch.countDown();

            boolean accepted = acceptFuture.get(30, TimeUnit.SECONDS);

            boolean expired = expireFuture.get(30, TimeUnit.SECONDS);

            assertFalse(
                    accepted && expired,
                    "A oferta não pode ser aceita e expirada simultaneamente"
            );

            SlotOffer persistedOffer =
                    slotOfferRepository
                            .findById(offerId)
                            .orElseThrow();

            AppointmentSlot persistedSlot =
                    appointmentSlotRepository
                            .findById(slot.getId())
                            .orElseThrow();

            WaitlistEntry persistedEntry =
                    waitlistEntryRepository
                            .findById(mariaEntry.getId())
                            .orElseThrow();

            if (accepted) {
                assertEquals(
                        SlotOfferStatus.ACCEPTED,
                        persistedOffer.getStatus()
                );
                assertEquals(
                        AppointmentSlotStatus.BOOKED,
                        persistedSlot.getStatus()
                );
                assertEquals(
                        WaitlistStatus.SCHEDULED,
                        persistedEntry.getStatus()
                );
            }

            if (expired) {
                assertEquals(
                        SlotOfferStatus.EXPIRED,
                        persistedOffer.getStatus()
                );
                assertEquals(
                        AppointmentSlotStatus.AVAILABLE,
                        persistedSlot.getStatus()
                );
                assertEquals(
                        WaitlistStatus.WAITING,
                        persistedEntry.getStatus()
                );
            }

        } finally {
            executor.shutdownNow();
            if (!executor.awaitTermination(
                    5,
                    TimeUnit.SECONDS
            )) {
                System.err.println("As threads não foram encerradas no prazo esperado");
            }
        }
    }


    @Test
    void shouldWaitForSlotLockBeforeAcceptingOffer() throws Exception {

        SlotOfferResponse response = slotOfferService.createOffer(slot.getId());

        UUID token = response.token();

        UUID offerId = slotOfferRepository
                .findByToken(token)
                .orElseThrow()
                .getId();

        CountDownLatch lockAcquired = new CountDownLatch(1);

        CountDownLatch releaseLock = new CountDownLatch(1);

        CountDownLatch acceptanceStarted = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<?> lockFuture = executor.submit(() -> {

                slotLockTestHelper.holdSlotLock(
                        slot.getId(),
                        lockAcquired,
                        releaseLock
                );
            });

            assertTrue(
                    lockAcquired.await(10, TimeUnit.SECONDS),
                    "A primeira transação não conseguiu bloquear a vaga"
            );

            Future<SlotOfferResponse> acceptanceFuture = executor.submit(() -> {
                        acceptanceStarted.countDown();
                        return slotOfferService.acceptOffer(token);
                    });

            assertTrue(
                    acceptanceStarted.await(10, TimeUnit.SECONDS),
                    "A operação de aceitação não iniciou"
            );

            assertThrows(
                    java.util.concurrent.TimeoutException.class,
                    () -> acceptanceFuture.get(
                            500,
                            TimeUnit.MILLISECONDS
                    )
            );

            releaseLock.countDown();
            lockFuture.get(10, TimeUnit.SECONDS);

            SlotOfferResponse acceptedResponse =
                    acceptanceFuture.get(
                            10,
                            TimeUnit.SECONDS
                    );

            assertEquals(
                    SlotOfferStatus.ACCEPTED,
                    acceptedResponse.status()
            );

            SlotOffer persistedOffer =
                    slotOfferRepository
                            .findById(offerId)
                            .orElseThrow();

            AppointmentSlot persistedSlot =
                    appointmentSlotRepository
                            .findById(slot.getId())
                            .orElseThrow();

            WaitlistEntry persistedEntry =
                    waitlistEntryRepository
                            .findById(mariaEntry.getId())
                            .orElseThrow();

            assertEquals(
                    SlotOfferStatus.ACCEPTED,
                    persistedOffer.getStatus()
            );
            assertEquals(
                    AppointmentSlotStatus.BOOKED,
                    persistedSlot.getStatus()
            );
            assertEquals(
                    WaitlistStatus.SCHEDULED,
                    persistedEntry.getStatus()
            );
        } finally {
            releaseLock.countDown();
            executor.shutdownNow();
            assertTrue(
                    executor.awaitTermination(
                            10,
                            TimeUnit.SECONDS
                    ),
                    "As threads não encerraram corretamente"
            );
        }
    }

    private String randomCode(String prefix) {
        return prefix
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 20);
    }
}