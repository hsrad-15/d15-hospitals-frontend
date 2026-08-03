package com.d15hospitals.backend.controller;

import com.d15hospitals.backend.dto.MedicalNoteRequest;
import com.d15hospitals.backend.entity.*;
import com.d15hospitals.backend.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalNoteRepository medicalNoteRepository;

    public DoctorController(DoctorRepository doctorRepository, AppointmentRepository appointmentRepository,
                            MedicalNoteRepository medicalNoteRepository) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.medicalNoteRepository = medicalNoteRepository;
    }

    private Doctor currentDoctor(Authentication authentication) {
        return doctorRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    @GetMapping("/profile")
    public Doctor viewProfile(Authentication authentication) {
        return currentDoctor(authentication);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Doctor updated, Authentication authentication) {
        Doctor doctor = currentDoctor(authentication);
        doctor.setName(updated.getName());
        doctor.setDepartment(updated.getDepartment());
        doctorRepository.save(doctor);
        return ResponseEntity.ok(Map.of("message", "Profile updated"));
    }

    @GetMapping("/appointments")
    public List<Appointment> myAppointments(Authentication authentication) {
        Doctor doctor = currentDoctor(authentication);
        return appointmentRepository.findByDoctorId(doctor.getId());
    }

    @PutMapping("/appointments/{id}/accept")
    public ResponseEntity<?> acceptAppointment(@PathVariable Long id, Authentication authentication) {
        Appointment appointment = getOwnedAppointment(id, authentication);
        appointment.setStatus(Appointment.Status.ACCEPTED);
        appointmentRepository.save(appointment);
        return ResponseEntity.ok(Map.of("message", "Appointment accepted"));
    }

    @PutMapping("/appointments/{id}/reject")
    public ResponseEntity<?> rejectAppointment(@PathVariable Long id, Authentication authentication) {
        Appointment appointment = getOwnedAppointment(id, authentication);
        appointment.setStatus(Appointment.Status.REJECTED);
        appointmentRepository.save(appointment);
        return ResponseEntity.ok(Map.of("message", "Appointment rejected"));
    }

    @PutMapping("/appointments/{id}/complete")
    public ResponseEntity<?> completeAppointment(@PathVariable Long id, Authentication authentication) {
        Appointment appointment = getOwnedAppointment(id, authentication);
        appointment.setStatus(Appointment.Status.COMPLETED);
        appointmentRepository.save(appointment);
        return ResponseEntity.ok(Map.of("message", "Appointment marked completed"));
    }

    @PostMapping("/appointments/{id}/notes")
    public ResponseEntity<?> addMedicalNote(@PathVariable Long id, @RequestBody MedicalNoteRequest request,
                                            Authentication authentication) {
        Appointment appointment = getOwnedAppointment(id, authentication);

        MedicalNote note = medicalNoteRepository.findByAppointmentId(id).orElse(new MedicalNote());
        note.setAppointment(appointment);
        note.setDiagnosis(request.getDiagnosis());
        note.setNotes(request.getNotes());
        medicalNoteRepository.save(note);

        return ResponseEntity.ok(Map.of("message", "Medical note saved"));
    }

    // helper - fetches an appointment AND verifies it belongs to the logged-in doctor
    private Appointment getOwnedAppointment(Long id, Authentication authentication) {
        Doctor doctor = currentDoctor(authentication);
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("This appointment doesn't belong to you");
        }
        return appointment;
    }
}