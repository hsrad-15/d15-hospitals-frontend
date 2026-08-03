package com.d15hospitals.backend.dto;

public class MedicalNoteRequest {
    private String diagnosis;
    private String notes;

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}