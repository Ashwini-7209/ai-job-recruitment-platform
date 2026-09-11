package com.jobplatform.resume.parsing;

import com.jobplatform.resume.enums.ResumeParsingStatus;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ResumeTextExtractorTests {

    private ResumeTextExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new ResumeTextExtractor(80000);
    }

    @Test
    void extractPdf_withText_returnsCompleted() throws IOException {
        byte[] pdfBytes = createPdfWithText("John Doe\nSoftware Engineer\nJava, Python, React");

        InputStream stream = new ByteArrayInputStream(pdfBytes);
        ExtractionResult result = extractor.extractText(stream, "application/pdf");

        assertThat(result.getStatus()).isEqualTo(ResumeParsingStatus.COMPLETED);
        assertThat(result.getExtractedText()).contains("John Doe");
        assertThat(result.getExtractedText()).contains("Software Engineer");
        assertThat(result.getExtractedText()).contains("Java");
    }

    @Test
    void extractPdf_empty_returnsScanRequired() throws IOException {
        byte[] pdfBytes = createPdfWithText("");

        InputStream stream = new ByteArrayInputStream(pdfBytes);
        ExtractionResult result = extractor.extractText(stream, "application/pdf");

        assertThat(result.getStatus()).isEqualTo(ResumeParsingStatus.SCAN_REQUIRED);
        assertThat(result.getFailureReason()).contains("no extractable text");
    }

    @Test
    void extractPdf_malformed_returnsFailed() {
        byte[] garbage = new byte[]{0x00, 0x01, 0x02, 0x03};

        InputStream stream = new ByteArrayInputStream(garbage);
        ExtractionResult result = extractor.extractText(stream, "application/pdf");

        assertThat(result.getStatus()).isEqualTo(ResumeParsingStatus.FAILED);
        assertThat(result.getFailureReason()).isNotBlank();
    }

    @Test
    void extractDocx_withText_returnsCompleted() throws IOException {
        byte[] docxBytes = createDocxWithText("Jane Smith\nProduct Manager\nAgile, Scrum");

        InputStream stream = new ByteArrayInputStream(docxBytes);
        ExtractionResult result = extractor.extractText(stream,
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        assertThat(result.getStatus()).isEqualTo(ResumeParsingStatus.COMPLETED);
        assertThat(result.getExtractedText()).contains("Jane Smith");
        assertThat(result.getExtractedText()).contains("Product Manager");
    }

    @Test
    void extractDocx_malformed_returnsFailed() {
        byte[] garbage = new byte[]{0x00, 0x01, 0x02, 0x03};

        InputStream stream = new ByteArrayInputStream(garbage);
        ExtractionResult result = extractor.extractText(stream,
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        assertThat(result.getStatus()).isEqualTo(ResumeParsingStatus.FAILED);
    }

    @Test
    void extractUnsupportedType_returnsFailed() {
        InputStream stream = new ByteArrayInputStream("test".getBytes());
        ExtractionResult result = extractor.extractText(stream, "image/png");

        assertThat(result.getStatus()).isEqualTo(ResumeParsingStatus.FAILED);
        assertThat(result.getFailureReason()).contains("Unsupported content type");
    }

    @Test
    void extractText_truncatesLongText() throws IOException {
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            longText.append("word ");
        }
        byte[] pdfBytes = createPdfWithText(longText.toString());

        ResumeTextExtractor shortExtractor = new ResumeTextExtractor(1000);
        InputStream stream = new ByteArrayInputStream(pdfBytes);
        ExtractionResult result = shortExtractor.extractText(stream, "application/pdf");

        assertThat(result.getStatus()).isEqualTo(ResumeParsingStatus.COMPLETED);
        assertThat(result.getExtractedText().length()).isLessThanOrEqualTo(1000);
    }

    @Test
    void getMaxTextLength_returnsConfiguredValue() {
        assertThat(extractor.getMaxTextLength()).isEqualTo(80000);
    }

    private byte[] createPdfWithText(String text) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new org.apache.pdfbox.pdmodel.font.PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.setLeading(14);
                contentStream.newLineAtOffset(50, 700);

                String[] lines = text.split("\n");
                for (String line : lines) {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -14);
                }

                contentStream.endText();
            }

            document.save(baos);
            return baos.toByteArray();
        }
    }

    private byte[] createDocxWithText(String text) throws IOException {
        org.apache.poi.xwpf.usermodel.XWPFDocument docx = new org.apache.poi.xwpf.usermodel.XWPFDocument();
        org.apache.poi.xwpf.usermodel.XWPFParagraph para = docx.createParagraph();
        org.apache.poi.xwpf.usermodel.XWPFRun run = para.createRun();
        run.setText(text);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        docx.write(baos);
        docx.close();
        return baos.toByteArray();
    }
}
