package com.jobplatform.resume.parsing;

import com.jobplatform.exception.BadRequestException;
import com.jobplatform.resume.enums.ResumeParsingStatus;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class ResumeTextExtractor {

    private static final Logger log = LoggerFactory.getLogger(ResumeTextExtractor.class);

    private final int maxTextLength;

    public ResumeTextExtractor(@Value("${app.resume.parsing.max-text-length:80000}") int maxTextLength) {
        this.maxTextLength = maxTextLength;
    }

    public ExtractionResult extractText(InputStream inputStream, String contentType) {
        try {
            byte[] bytes = inputStream.readAllBytes();
            return switch (contentType) {
                case "application/pdf" -> extractPdf(bytes);
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> extractDocx(bytes);
                case "application/msword" -> extractDoc(bytes);
                default -> ExtractionResult.builder()
                        .status(ResumeParsingStatus.FAILED)
                        .failureReason("Unsupported content type: " + contentType)
                        .build();
            };
        } catch (IOException e) {
            log.error("Failed to read input stream", e);
            return ExtractionResult.builder()
                    .status(ResumeParsingStatus.FAILED)
                    .failureReason("Failed to read document: " + e.getMessage())
                    .build();
        }
    }

    private ExtractionResult extractPdf(byte[] bytes) {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            if (text == null || text.isBlank()) {
                return ExtractionResult.builder()
                        .status(ResumeParsingStatus.SCAN_REQUIRED)
                        .failureReason("PDF contains no extractable text (likely scanned/image-based)")
                        .build();
            }

            text = truncateText(text);

            return ExtractionResult.builder()
                    .status(ResumeParsingStatus.COMPLETED)
                    .extractedText(text)
                    .build();
        } catch (IOException e) {
            log.error("Failed to extract text from PDF", e);
            return ExtractionResult.builder()
                    .status(ResumeParsingStatus.FAILED)
                    .failureReason("Failed to parse PDF: " + e.getMessage())
                    .build();
        }
    }

    private ExtractionResult extractDocx(byte[] bytes) {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            String text = extractor.getText();

            if (text == null || text.isBlank()) {
                return ExtractionResult.builder()
                        .status(ResumeParsingStatus.FAILED)
                        .failureReason("DOCX document contains no extractable text")
                        .build();
            }

            text = truncateText(text);

            return ExtractionResult.builder()
                    .status(ResumeParsingStatus.COMPLETED)
                    .extractedText(text)
                    .build();
        } catch (Exception e) {
            log.error("Failed to extract text from DOCX", e);
            return ExtractionResult.builder()
                    .status(ResumeParsingStatus.FAILED)
                    .failureReason("Failed to parse DOCX: " + e.getMessage())
                    .build();
        }
    }

    private ExtractionResult extractDoc(byte[] bytes) {
        try (org.apache.poi.hwpf.HWPFDocument doc = new org.apache.poi.hwpf.HWPFDocument(new ByteArrayInputStream(bytes))) {
            org.apache.poi.hwpf.extractor.WordExtractor extractor = new org.apache.poi.hwpf.extractor.WordExtractor(doc);
            String text = extractor.getText();

            if (text == null || text.isBlank()) {
                return ExtractionResult.builder()
                        .status(ResumeParsingStatus.FAILED)
                        .failureReason("DOC document contains no extractable text")
                        .build();
            }

            text = truncateText(text);

            return ExtractionResult.builder()
                    .status(ResumeParsingStatus.COMPLETED)
                    .extractedText(text)
                    .build();
        } catch (Exception e) {
            log.error("Failed to extract text from DOC", e);
            return ExtractionResult.builder()
                    .status(ResumeParsingStatus.FAILED)
                    .failureReason("Failed to parse DOC: " + e.getMessage())
                    .build();
        }
    }

    private String truncateText(String text) {
        if (text.length() > maxTextLength) {
            return text.substring(0, maxTextLength);
        }
        return text;
    }

    public int getMaxTextLength() {
        return maxTextLength;
    }
}
