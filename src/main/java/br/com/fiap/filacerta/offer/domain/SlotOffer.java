package br.com.fiap.filacerta.offer.domain;

import br.com.fiap.filacerta.scheduling.domain.AppointmentSlot;
import br.com.fiap.filacerta.shared.exception.BusinessException;
import br.com.fiap.filacerta.waitlist.domain.WaitlistEntry;
import jakarta.persistence.*;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "slot_offer")
public class SlotOffer {

    private static final long EXPIRATION_MINUTES = 15;

    @Id
    private UUID id;

    @Column(name = "token", nullable = false, unique = true)
    private UUID token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_slot_id", nullable = false)
    private AppointmentSlot appointmentSlot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "waitlist_entry_id", nullable = false)
    private WaitlistEntry waitlistEntry;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SlotOfferStatus status;

    @Column(name = "offered_at", nullable = false, updatable = false)
    private OffsetDateTime offeredAt;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "responded_at")
    private OffsetDateTime respondedAt;

    protected SlotOffer() {
    }


    public SlotOffer(
            AppointmentSlot appointmentSlot,
            WaitlistEntry waitlistEntry,
            Clock clock
    ) {
        this.appointmentSlot = Objects.requireNonNull(appointmentSlot, "A vaga é obrigatória");

        this.waitlistEntry = Objects.requireNonNull(waitlistEntry, "A entrada da fila é obrigatória");

        Objects.requireNonNull(clock, "O relógio é obrigatório");

        OffsetDateTime now = OffsetDateTime.now(clock);

        this.id = UUID.randomUUID();
        this.token = UUID.randomUUID();

        this.status = SlotOfferStatus.PENDING;

        this.offeredAt = now;
        this.expiresAt = now.plusMinutes(EXPIRATION_MINUTES);

        this.respondedAt = null;
    }

    public void accept(Clock clock){
        OffsetDateTime now = OffsetDateTime.now(clock);
        validatePending();
        if(!now.isBefore(this.expiresAt)){
            throw new BusinessException("Não é possível aceitar uma oferta expirada");
        }
        this.status = SlotOfferStatus.ACCEPTED;
        this.respondedAt = now;
    }

    public void reject(Clock clock){
        OffsetDateTime now = OffsetDateTime.now(clock);
        validatePending();
        if(!now.isBefore(this.expiresAt)){
            throw new BusinessException("Não é possível rejeitar uma oferta expirada");
        }
        this.status = SlotOfferStatus.REJECTED;
        this.respondedAt = now;
    }

    public void expire(Clock clock){
        OffsetDateTime now = OffsetDateTime.now(clock);
        validatePending();
        if(now.isBefore(this.expiresAt)){
            throw new BusinessException("A oferta ainda está dentro do prazo de resposta");
        }
        this.status = SlotOfferStatus.EXPIRED;
        this.respondedAt = null;
    }

    private void validatePending(){
        if(this.status != SlotOfferStatus.PENDING){
            throw new BusinessException("A oferta não está pendente");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getToken() {
        return token;
    }

    public AppointmentSlot getAppointmentSlot() {
        return appointmentSlot;
    }

    public WaitlistEntry getWaitlistEntry() {
        return waitlistEntry;
    }

    public SlotOfferStatus getStatus() {
        return status;
    }

    public OffsetDateTime getOfferedAt() {
        return offeredAt;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public OffsetDateTime getRespondedAt() {
        return respondedAt;
    }
}