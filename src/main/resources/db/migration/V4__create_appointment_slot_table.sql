CREATE TABLE appointment_slot (
    id UUID PRIMARY KEY,

    health_unit_id UUID NOT NULL,
    specialty_id UUID NOT NULL,

    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_appointment_slot_health_unit
        FOREIGN KEY (health_unit_id)
        REFERENCES health_unit(id),

    CONSTRAINT fk_appointment_slot_specialty
        FOREIGN KEY (specialty_id)
        REFERENCES specialty(id)
);