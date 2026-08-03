package com.d15hospitals.backend.controller;

import com.d15hospitals.backend.entity.*;
import com.d15hospitals.backend.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(DoctorRepository doctorRepository, PatientRepository patientRepository,
                           AppointmentRepository appointmentRepository, PasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------- Dashboard ----------
    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return Map.of(
                "totalDoctors", doctorRepository.count(),
                "totalPatients", patientRepository.count(),
                "totalAppointments", appointmentRepository.count()
        );
    }

    // ---------- Doctor management ----------
    @GetMapping("/doctors")
    public List<Doctor> viewDoctors() {
        return doctorRepository.findAll();
    }

    @PostMapping("/doctors")
    public ResponseEntity<?> addDoctor(@RequestBody Doctor doctor) {
        if (doctorRepository.findByEmail(doctor.getEmail()).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "Email already exists"));
        }
        doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
        doctorRepository.save(doctor);
        return ResponseEntity.ok(Map.of("message", "Doctor added", "id", doctor.getId()));
    }

    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<?> removeDoctor(@PathVariable Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        doctor.setActive(false); // soft delete - keeps history intact
        doctorRepository.save(doctor);
        return ResponseEntity.ok(Map.of("message", "Doctor deactivated"));
    }

    // ---------- Patient viewing ----------
    @GetMapping("/patients")
    public List<Patient> viewPatients() {
        return patientRepository.findAll();
    }

    // ---------- Appointment viewing ----------
    @GetMapping("/appointments")
    public List<Appointment> viewAppointments(@RequestParam(required = false) String status) {
        List<Appointment> all = appointmentRepository.findAll();
        if (status != null) {
            return all.stream()
                    .filter(a -> a.getStatus().name().equalsIgnoreCase(status))
                    .toList();
        }
        return all;
    }
}