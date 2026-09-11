package com.jobplatform.resume;

import com.jobplatform.exception.BadRequestException;
import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.resume.dto.ResumeResponse;
import com.jobplatform.resume.storage.FileStorageService;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ResumeService {

    private static final Logger log = LoggerFactory.getLogger(ResumeService.class);

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx");

    private static final Map<String, String> MIME_TYPE_MAP = Map.of(
            "pdf", "application/pdf",
            "doc", "application/msword",
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private static final byte[] PDF_SIGNATURE = {(byte) 0x25, (byte) 0x50, (byte) 0x44, (byte) 0x46};
    private static final byte[] DOC_SIGNATURE = {(byte) 0xD0, (byte) 0xCF, (byte) 0x11, (byte) 0xE0};
    private static final byte[] DOCX_SIGNATURE_1 = {(byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04};

    private final ResumeRepository resumeRepository;
    private final FileStorageService fileStorageService;

    public ResumeService(ResumeRepository resumeRepository, FileStorageService fileStorageService) {
        this.resumeRepository = resumeRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public ResumeResponse uploadResume(User candidate, String originalFileName, String contentType, byte[] data) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can upload resumes");
        }

        validateFile(originalFileName, contentType, data);

        String extension = getFileExtension(originalFileName);
        String storedFileName = UUID.randomUUID() + "." + extension;

        String storagePath = fileStorageService.store(data, storedFileName);

        Resume resume = Resume.builder()
                .candidate(candidate)
                .originalFileName(sanitizeFileName(originalFileName))
                .storedFileName(storedFileName)
                .contentType(contentType)
                .fileSize((long) data.length)
                .active(false)
                .build();

        Resume saved = resumeRepository.save(resume);

        long resumeCount = resumeRepository.countByCandidate(candidate);
        if (resumeCount == 1) {
            saved.setActive(true);
            saved = resumeRepository.save(saved);
        }

        log.info("Resume uploaded: candidate={}, resumeId={}, fileName={}", candidate.getEmail(), saved.getId(), originalFileName);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> getCandidateResumes(User candidate) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can view their resumes");
        }

        return resumeRepository.findByCandidateOrderByCreatedAtDesc(candidate)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumeResponse getResumeById(User candidate, Long resumeId) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can view resume details");
        }

        Resume resume = resumeRepository.findByIdAndCandidate(resumeId, candidate)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "id", resumeId));

        return mapToResponse(resume);
    }

    @Transactional
    public InputStream downloadResume(User candidate, Long resumeId) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can download their resumes");
        }

        Resume resume = resumeRepository.findByIdAndCandidate(resumeId, candidate)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "id", resumeId));

        return fileStorageService.load(resume.getStoredFileName())
                .orElseThrow(() -> new BadRequestException("Resume file not found on disk"));
    }

    @Transactional
    public ResumeResponse activateResume(User candidate, Long resumeId) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can activate resumes");
        }

        Resume resume = resumeRepository.findByIdAndCandidate(resumeId, candidate)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "id", resumeId));

        resumeRepository.deactivateAllByCandidate(candidate);

        resume.setActive(true);
        Resume saved = resumeRepository.save(resume);

        log.info("Resume activated: candidate={}, resumeId={}", candidate.getEmail(), resumeId);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteResume(User candidate, Long resumeId) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can delete resumes");
        }

        Resume resume = resumeRepository.findByIdAndCandidate(resumeId, candidate)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "id", resumeId));

        boolean wasActive = Boolean.TRUE.equals(resume.getActive());

        fileStorageService.delete(resume.getStoredFileName());
        resumeRepository.delete(resume);

        if (wasActive) {
            List<Resume> remaining = resumeRepository.findByCandidateOrderByCreatedAtDesc(candidate);
            if (!remaining.isEmpty()) {
                Resume newest = remaining.getFirst();
                newest.setActive(true);
                resumeRepository.save(newest);
                log.info("Newest remaining resume activated after deletion: resumeId={}", newest.getId());
            }
        }

        log.info("Resume deleted: candidate={}, resumeId={}", candidate.getEmail(), resumeId);
    }

    public Optional<Resume> getResumeForApplication(User candidate) {
        List<Resume> active = resumeRepository.findByCandidateAndActiveTrue(candidate);
        return active.stream().findFirst();
    }

    public Optional<InputStream> getResumeStream(String storedFileName) {
        return fileStorageService.load(storedFileName);
    }

    private void validateFile(String originalFileName, String contentType, byte[] data) {
        if (data == null || data.length == 0) {
            throw new BadRequestException("File is empty");
        }

        if (data.length > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum limit of 10MB");
        }

        String extension = getFileExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BadRequestException("Unsupported file type. Allowed: PDF, DOC, DOCX");
        }

        if (!dataValidateSignature(data, extension)) {
            throw new BadRequestException("File content does not match the expected type");
        }
    }

    private boolean dataValidateSignature(byte[] data, String extension) {
        if (data.length < 4) {
            return false;
        }

        return switch (extension.toLowerCase()) {
            case "pdf" -> startsWith(data, PDF_SIGNATURE);
            case "doc" -> startsWith(data, DOC_SIGNATURE);
            case "docx" -> startsWith(data, DOCX_SIGNATURE_1);
            default -> false;
        };
    }

    private boolean startsWith(byte[] data, byte[] signature) {
        if (data.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (data[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "resume";
        }
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_").substring(0, Math.min(fileName.length(), 100));
    }

    private ResumeResponse mapToResponse(Resume resume) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .originalFileName(resume.getOriginalFileName())
                .contentType(resume.getContentType())
                .fileSize(resume.getFileSize())
                .active(resume.getActive())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();
    }
}
