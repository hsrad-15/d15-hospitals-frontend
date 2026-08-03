package com.d15hospitals.backend.repository;

import com.d15hospitals.backend.entity.MedicalNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MedicalNoteRepository extends JpaRepository<MedicalNote, Long> {
    Optional<MedicalNote> findByAppointmentId(Long appointmentId);
}