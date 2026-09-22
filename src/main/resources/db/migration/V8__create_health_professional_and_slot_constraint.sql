
CREATE TABLE health_professional (

    id UUID PRIMARY KEY,

    name VARCHAR(150) NOT NULL,

    professional_registration VARCHAR(50) NOT NULL,

    health_unit_id UUID NOT NULL,

    specialty_id UUID NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_health_professional_registration
        UNIQUE (professional_registration),

    CONSTRAINT fk_health_professional_health_unit
        FOREIGN KEY (health_unit_id)
        REFERENCES health_unit(id),

    CONSTRAINT fk_health_professional_specialty
        FOREIGN KEY (specialty_id)
        REFERENCES specialty(id)
);


ALTER TABLE appointment_slot
ADD COLUMN professional_id UUID;


ALTER TABLE appointment_slot
ADD CONSTRAINT fk_appointment_slot_professional
FOREIGN KEY (professional_id)
REFERENCES health_professional(id);


CREATE UNIQUE INDEX uq_appointment_slot_professional_scheduled_at
ON appointment_slot (
    professional_id,
    scheduled_at
)
WHERE professional_id IS NOT NULL;