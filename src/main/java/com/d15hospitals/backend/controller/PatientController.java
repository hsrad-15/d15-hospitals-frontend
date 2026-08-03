package com.d15hospitals.backend.controller;

import com.d15hospitals.backend.dto.BookAppointmentRequest;
import com.d15hospitals.backend.entity.*;
import com.d15hospitals.backend.repository.*;
import com.d15hospitals.backend.service.PdfService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalNoteRepository medicalNoteRepository;
    private final PdfService pdfService;

    public PatientController(PatientRepository patientRepository, DoctorRepository doctorRepository,
                             AppointmentRepository appointmentRepository, MedicalNoteRepository medicalNoteRepository,
                             PdfService pdfService) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.medicalNoteRepository = medicalNoteRepository;
        this.pdfService = pdfService;
    }

    @GetMapping("/doctors")
    public List<Doctor> viewDoctors(@RequestParam(required = false) String department) {
        if (department != null) {
            return doctorRepository.findAll().stream()
                    .filter(d -> d.getDepartment().equalsIgnoreCase(department))
                    .toList();
        }
        return doctorRepository.findAll();
    }

    @PostMapping("/book-appointment")
    public ResponseEntity<?> bookAppointment(@RequestBody BookAppointmentRequest request,
                                             Authentication authentication) {
        String email = authentication.getName();
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        boolean slotTaken = appointmentRepository
                .existsByDoctorIdAndAppointmentTime(doctor.getId(), request.getAppointmentTime());
        if (slotTaken) {
            return ResponseEntity.status(409).body(Map.of("error", "This slot is already booked"));
        }

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointmentRepository.save(appointment);

        return ResponseEntity.ok(Map.of("message", "Appointment booked", "id", appointment.getId()));
    }

    @GetMapping("/appointments")
    public List<Appointment> myAppointments(Authentication authentication) {
        Patient patient = patientRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        return appointmentRepository.findByPatientId(patient.getId());
    }

    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id, Authentication authentication) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getPatient().getEmail().equals(authentication.getName())) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your appointment"));
        }

        appointmentRepository.delete(appointment);
        return ResponseEntity.ok(Map.of("message", "Appointment cancelled"));
    }

    @GetMapping("/appointments/{id}/report/download")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long id, Authentication authentication) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getPatient().getEmail().equals(authentication.getName())) {
            return ResponseEntity.status(403).build();
        }

        MedicalNote note = medicalNoteRepository.findByAppointmentId(id)
                .orElseThrow(() -> new RuntimeException("No report available for this appointment yet"));

        byte[] pdfBytes = pdfService.generateReportPdf(appointment, note);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=report_" + id + ".pdf")
                .body(pdfBytes);
    }
}