package com.d15hospitals.backend.controller;

import com.d15hospitals.backend.dto.LoginRequest;
import com.d15hospitals.backend.dto.RegisterRequest;
import com.d15hospitals.backend.entity.Doctor;
import com.d15hospitals.backend.entity.Patient;
import com.d15hospitals.backend.repository.*;
import com.d15hospitals.backend.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(AdminRepository adminRepository, DoctorRepository doctorRepository,
                          PatientRepository patientRepository, PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        var admin = adminRepository.findByEmail(request.getEmail());
        if (admin.isPresent() && passwordEncoder.matches(request.getPassword(), admin.get().getPassword())) {
            String token = jwtUtil.generateToken(admin.get().getEmail(), admin.get().getId(), "ADMIN");
            return ResponseEntity.ok(Map.of("token", token, "role", "ADMIN"));
        }

        var doctor = doctorRepository.findByEmail(request.getEmail());
        if (doctor.isPresent() && passwordEncoder.matches(request.getPassword(), doctor.get().getPassword())) {
            String token = jwtUtil.generateToken(doctor.get().getEmail(), doctor.get().getId(), "DOCTOR");
            return ResponseEntity.ok(Map.of("token", token, "role", "DOCTOR"));
        }

        var patient = patientRepository.findByEmail(request.getEmail());
        if (patient.isPresent() && passwordEncoder.matches(request.getPassword(), patient.get().getPassword())) {
            String token = jwtUtil.generateToken(patient.get().getEmail(), patient.get().getId(), "PATIENT");
            return ResponseEntity.ok(Map.of("token", token, "role", "PATIENT"));
        }

        return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
    }

    @PostMapping("/register/patient")
    public ResponseEntity<?> registerPatient(@RequestBody RegisterRequest request) {
        if (patientRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "Email already registered"));
        }
        Patient patient = new Patient();
        patient.setName(request.getName());
        patient.setEmail(request.getEmail());
        patient.setPassword(passwordEncoder.encode(request.getPassword()));
        patient.setPhone(request.getPhone());
        patientRepository.save(patient);
        return ResponseEntity.ok(Map.of("message", "Patient registered successfully"));
    }

    @PostMapping("/register/doctor")
    public ResponseEntity<?> registerDoctor(@RequestBody RegisterRequest request) {
        if (doctorRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "Email already registered"));
        }
        Doctor doctor = new Doctor();
        doctor.setName(request.getName());
        doctor.setEmail(request.getEmail());
        doctor.setPassword(passwordEncoder.encode(request.getPassword()));
        doctor.setDepartment(request.getDepartment());
        doctorRepository.save(doctor);
        return ResponseEntity.ok(Map.of("message", "Doctor registered successfully"));
    }
}