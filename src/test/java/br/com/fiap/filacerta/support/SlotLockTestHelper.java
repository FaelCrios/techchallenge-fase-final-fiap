package br.com.fiap.filacerta.support;

import br.com.fiap.filacerta.scheduling.infrastructure.AppointmentSlotRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Component
public class SlotLockTestHelper {

    private final AppointmentSlotRepository appointmentSlotRepository;

    private final TransactionTemplate transactionTemplate;

    public SlotLockTestHelper(
            AppointmentSlotRepository appointmentSlotRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.appointmentSlotRepository = appointmentSlotRepository;

        this.transactionTemplate =
                new TransactionTemplate(transactionManager);
    }

    public void holdSlotLock(
            UUID slotId,
            CountDownLatch lockAcquired,
            CountDownLatch releaseLock
    ) {

        transactionTemplate.executeWithoutResult(status -> {

            appointmentSlotRepository
                    .findByIdForUpdate(slotId)
                    .orElseThrow(() -> new IllegalStateException("Vaga não encontrada: " + slotId));

            lockAcquired.countDown();

            try {
                boolean released = releaseLock.await(
                        20,
                        TimeUnit.SECONDS
                );
                if (!released) {
                    throw new IllegalStateException(
                            "Tempo limite para liberar o bloqueio"
                    );
                }
            } catch (InterruptedException exception) {

                Thread.currentThread().interrupt();

                throw new IllegalStateException("Thread interrompida durante o bloqueio", exception);
            }
        });
    }
}