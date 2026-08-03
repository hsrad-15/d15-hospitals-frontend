package com.d15hospitals.backend.dto;

import java.time.LocalDateTime;

public class BookAppointmentRequest {
    private Long doctorId;
    private LocalDateTime appointmentTime;

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalDateTime t) { this.appointmentTime = t; }
}