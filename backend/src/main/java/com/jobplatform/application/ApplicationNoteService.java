package com.jobplatform.application;

import com.jobplatform.application.dto.ApplicationNoteRequest;
import com.jobplatform.application.dto.ApplicationNoteResponse;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.exception.BadRequestException;
import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicationNoteService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationNoteService.class);
    private static final int MAX_PAGE_SIZE = 50;

    private final ApplicationNoteRepository noteRepository;
    private final ApplicationRepository applicationRepository;

    public ApplicationNoteService(ApplicationNoteRepository noteRepository, ApplicationRepository applicationRepository) {
        this.noteRepository = noteRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public ApplicationNoteResponse createNote(User recruiter, Long applicationId, ApplicationNoteRequest request) {
        if (recruiter.getRole() != UserRole.RECRUITER) {
            throw new BadRequestException("Only recruiters can add notes");
        }

        Application application = findAndVerifyOwnership(applicationId, recruiter);

        ApplicationNote note = ApplicationNote.builder()
                .application(application)
                .recruiter(recruiter)
                .note(request.getNote().trim())
                .build();

        ApplicationNote saved = noteRepository.save(note);
        log.info("Note created: recruiter={} application={}", recruiter.getEmail(), applicationId);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ApplicationNoteResponse> getNotes(User recruiter, Long applicationId) {
        if (recruiter.getRole() != UserRole.RECRUITER) {
            throw new BadRequestException("Only recruiters can view notes");
        }

        findAndVerifyOwnership(applicationId, recruiter);

        return noteRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId)
                .stream()
                .filter(n -> n.getRecruiter().getId().equals(recruiter.getId()))
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public ApplicationNoteResponse updateNote(User recruiter, Long applicationId, Long noteId, ApplicationNoteRequest request) {
        if (recruiter.getRole() != UserRole.RECRUITER) {
            throw new BadRequestException("Only recruiters can update notes");
        }

        findAndVerifyOwnership(applicationId, recruiter);

        ApplicationNote note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", noteId));

        if (!note.getRecruiter().getId().equals(recruiter.getId())) {
            throw new BadRequestException("You do not have permission to modify this note");
        }

        if (!note.getApplication().getId().equals(applicationId)) {
            throw new BadRequestException("Note does not belong to this application");
        }

        note.setNote(request.getNote().trim());
        ApplicationNote saved = noteRepository.save(note);
        log.info("Note updated: noteId={} recruiter={}", noteId, recruiter.getEmail());
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteNote(User recruiter, Long applicationId, Long noteId) {
        if (recruiter.getRole() != UserRole.RECRUITER) {
            throw new BadRequestException("Only recruiters can delete notes");
        }

        findAndVerifyOwnership(applicationId, recruiter);

        ApplicationNote note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", noteId));

        if (!note.getRecruiter().getId().equals(recruiter.getId())) {
            throw new BadRequestException("You do not have permission to delete this note");
        }

        if (!note.getApplication().getId().equals(applicationId)) {
            throw new BadRequestException("Note does not belong to this application");
        }

        noteRepository.delete(note);
        log.info("Note deleted: noteId={} recruiter={}", noteId, recruiter.getEmail());
    }

    @Transactional(readOnly = true)
    public long countNotesForApplication(Long applicationId) {
        return noteRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId).size();
    }

    private Application findAndVerifyOwnership(Long applicationId, User recruiter) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        if (!application.getJob().getRecruiter().getId().equals(recruiter.getId())) {
            throw new BadRequestException("You do not have permission to access this application");
        }

        return application;
    }

    private ApplicationNoteResponse mapToResponse(ApplicationNote note) {
        return ApplicationNoteResponse.builder()
                .id(note.getId())
                .note(note.getNote())
                .recruiterName(note.getRecruiter().getFullName())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
