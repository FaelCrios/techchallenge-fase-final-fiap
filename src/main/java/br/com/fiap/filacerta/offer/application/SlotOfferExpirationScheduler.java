package br.com.fiap.filacerta.offer.application;

import br.com.fiap.filacerta.offer.domain.SlotOfferStatus;
import br.com.fiap.filacerta.offer.infrastructure.SlotOfferRepository;
import br.com.fiap.filacerta.shared.exception.BusinessException;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;

@Component
@ConditionalOnProperty(
        name = "filacerta.offer.expiration.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class SlotOfferExpirationScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlotOfferExpirationScheduler.class);

    private final SlotOfferRepository slotOfferRepository;

    private final SlotOfferExpirationService expirationService;

    private final SlotOfferService slotOfferService;

    private final Clock clock;

    public SlotOfferExpirationScheduler(SlotOfferRepository slotOfferRepository, SlotOfferExpirationService expirationService, SlotOfferService slotOfferService, Clock clock) {
        this.slotOfferRepository = slotOfferRepository;
        this.expirationService = expirationService;
        this.slotOfferService = slotOfferService;
        this.clock = clock;
    }

    @Scheduled(fixedDelay = 300000)
    public void processExpiredOffers(){
        OffsetDateTime now = OffsetDateTime.now(clock);

        List<UUID> expiredOfferIds = slotOfferRepository.findExpiredOfferIds(SlotOfferStatus.PENDING,now);
        LOGGER.info("Iniciando processamento de {} ofertas expiradas", expiredOfferIds.size());

        for( UUID offerId : expiredOfferIds){
            try{
                Optional<UUID> releasedSlotId = expirationService.expireOffer(offerId);
                if(releasedSlotId.isEmpty()){
                    continue;
                }
                UUID slotId = releasedSlotId.get();
                try {
                    slotOfferService.createOffer(slotId);
                    LOGGER.info("Nova oferta criada para a vaga {}",slotId);
                } catch (BusinessException e) {
                    LOGGER.info("Vaga {} liberada sem nova oferta: {}", slotId, e.getMessage());
                }
            } catch (Exception e){
                LOGGER.error("Erro ao processar expiração da oferta {}", offerId, e);
            }
        }
        LOGGER.info("Processamento de ofertas expiradas finalizado");
    }
}
