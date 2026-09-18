CREATE TABLE waitlist_entry (
    id UUID PRIMARY KEY,

    patient_id UUID NOT NULL,
    health_unit_id UUID NOT NULL,
    specialty_id UUID NOT NULL,

    clinical_priority VARCHAR(30) NOT NULL,
    preferred_period VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,

    entered_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_waitlist_patient
        FOREIGN KEY (patient_id)
        REFERENCES patient(id),

    CONSTRAINT fk_waitlist_health_unit
        FOREIGN KEY (health_unit_id)
        REFERENCES health_unit(id),

    CONSTRAINT fk_waitlist_specialty
        FOREIGN KEY (specialty_id)
        REFERENCES specialty(id)
);