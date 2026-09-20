CREATE TABLE slot_offer (
    id UUID PRIMARY KEY,

    token UUID NOT NULL UNIQUE,

    appointment_slot_id UUID NOT NULL,
    waitlist_entry_id UUID NOT NULL,

    status VARCHAR(30) NOT NULL,

    offered_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    responded_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_slot_offer_appointment_slot
        FOREIGN KEY (appointment_slot_id)
        REFERENCES appointment_slot(id),

    CONSTRAINT fk_slot_offer_waitlist_entry
        FOREIGN KEY (waitlist_entry_id)
        REFERENCES waitlist_entry(id)
);