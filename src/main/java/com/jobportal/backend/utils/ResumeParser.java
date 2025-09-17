
package com.jobportal.backend.utils;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

@Component
public class ResumeParser {

    private final Tika tika = new Tika();
    private final Tesseract tesseract = new Tesseract();

    public ResumeParser() {
        // Optional: set Tesseract data path if needed
        // tesseract.setDatapath("tessdata");
        tesseract.setLanguage("eng");
    }

    public String parse(MultipartFile file) {
        if (file == null || file.isEmpty()) return "";

        try {
            // Read file into bytes to allow multiple reads
            byte[] fileBytes = file.getBytes();

            // First try Tika
            String text = tika.parseToString(new ByteArrayInputStream(fileBytes));
            if (text != null && !text.trim().isEmpty()) {
                return text.trim();
            }

            // If Tika failed (scanned PDF), fallback to OCR
            File tempFile = File.createTempFile("resume", ".pdf");
            file.transferTo(tempFile); // save MultipartFile as temp File
            String txt = tesseract.doOCR(tempFile);
            tempFile.delete(); // clean up
            return txt;
        } catch (TesseractException e) {
            // OCR failed
            return "";
        } catch (Exception e) {
            // Tika failed
            return "";
        }
    }
    public String parse(InputStream inputStream) {
        try {
            return tika.parseToString(inputStream);
        } catch (Exception e) {
            return "";
        }
    }

    public String parseFromUrl(String fileUrl) {
        try (InputStream inputStream = new URL(fileUrl).openStream()) {
            return tika.parseToString(inputStream);
        } catch (Exception e) {
            return "";
        }
    }
}