CREATE UNIQUE INDEX uq_slot_offer_pending_slot
ON slot_offer (appointment_slot_id)
WHERE status = 'PENDING';

CREATE UNIQUE INDEX uq_slot_offer_pending_entry
ON slot_offer (waitlist_entry_id)
WHERE status = 'PENDING';

CREATE INDEX idx_slot_offer_pending_expiration
ON slot_offer (expires_at)
WHERE status = 'PENDING';