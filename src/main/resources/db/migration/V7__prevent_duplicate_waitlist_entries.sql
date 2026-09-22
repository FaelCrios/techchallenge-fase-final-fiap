CREATE UNIQUE INDEX uq_waitlist_active_patient_unit_specialty
ON waitlist_entry (
    patient_id,
    health_unit_id,
    specialty_id
)
WHERE status IN (
    'WAITING',
    'OFFERED',
    'SCHEDULED'
);