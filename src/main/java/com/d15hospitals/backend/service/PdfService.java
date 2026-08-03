package com.d15hospitals.backend.service;

import com.d15hospitals.backend.entity.Appointment;
import com.d15hospitals.backend.entity.MedicalNote;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    public byte[] generateReportPdf(Appointment appointment, MedicalNote note) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font labelFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 12, Font.NORMAL);

            document.add(new Paragraph("D15 Hospitals - Medical Report", titleFont));
            document.add(new Paragraph(" "));

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

            document.add(new Paragraph("Patient: " + appointment.getPatient().getName(), labelFont));
            document.add(new Paragraph("Doctor: Dr. " + appointment.getDoctor().getName(), labelFont));
            document.add(new Paragraph("Department: " + appointment.getDoctor().getDepartment(), normalFont));
            document.add(new Paragraph("Appointment Date: " + appointment.getAppointmentTime().format(fmt), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Diagnosis:", labelFont));
            document.add(new Paragraph(note.getDiagnosis() != null ? note.getDiagnosis() : "N/A", normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Doctor's Notes:", labelFont));
            document.add(new Paragraph(note.getNotes() != null ? note.getNotes() : "N/A", normalFont));

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }

        return out.toByteArray();
    }
}