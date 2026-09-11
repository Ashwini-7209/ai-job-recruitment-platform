package com.jobplatform.application;

import com.jobplatform.application.dto.ApplicationNoteRequest;
import com.jobplatform.application.dto.ApplicationNoteResponse;
import com.jobplatform.exception.BadRequestException;
import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.job.Job;
import com.jobplatform.job.enums.JobStatus;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationNoteServiceTests {

    @Mock
    private ApplicationNoteRepository noteRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private ApplicationNoteService noteService;

    private User recruiter;
    private User otherRecruiter;
    private Application application;

    @BeforeEach
    void setUp() {
        recruiter = User.builder().id(1L).role(UserRole.RECRUITER).fullName("Recruiter").build();
        otherRecruiter = User.builder().id(2L).role(UserRole.RECRUITER).fullName("Other").build();

        Job job = Job.builder().id(10L).recruiter(recruiter).status(JobStatus.PUBLISHED).build();
        application = Application.builder().id(100L).job(job).build();
    }

    @Test
    void createNote_success() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));
        when(noteRepository.save(any())).thenAnswer(inv -> {
            ApplicationNote note = inv.getArgument(0);
            note.setId(1L);
            note.setCreatedAt(LocalDateTime.now());
            note.setUpdatedAt(LocalDateTime.now());
            return note;
        });

        ApplicationNoteResponse response = noteService.createNote(recruiter, 100L,
                ApplicationNoteRequest.builder().note("Test note").build());

        assertThat(response.getNote()).isEqualTo("Test note");
        verify(noteRepository).save(any());
    }

    @Test
    void createNote_notRecruiter_throws() {
        User candidate = User.builder().id(3L).role(UserRole.CANDIDATE).build();
        assertThatThrownBy(() -> noteService.createNote(candidate, 100L,
                ApplicationNoteRequest.builder().note("Test").build()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createNote_wrongRecruiter_throws() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));
        assertThatThrownBy(() -> noteService.createNote(otherRecruiter, 100L,
                ApplicationNoteRequest.builder().note("Test").build()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createNote_applicationNotFound_throws() {
        when(applicationRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> noteService.createNote(recruiter, 999L,
                ApplicationNoteRequest.builder().note("Test").build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getNotes_onlyRecruiterNotesReturned() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        ApplicationNote myNote = ApplicationNote.builder().id(1L).recruiter(recruiter).note("My note").build();
        ApplicationNote otherNote = ApplicationNote.builder().id(2L).recruiter(otherRecruiter).note("Other note").build();

        when(noteRepository.findByApplicationIdOrderByCreatedAtDesc(100L))
                .thenReturn(List.of(myNote, otherNote));

        List<ApplicationNoteResponse> notes = noteService.getNotes(recruiter, 100L);

        assertThat(notes).hasSize(1);
        assertThat(notes.get(0).getNote()).isEqualTo("My note");
    }

    @Test
    void updateNote_success() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        ApplicationNote existingNote = ApplicationNote.builder()
                .id(1L).recruiter(recruiter).application(application).note("Old note").build();
        when(noteRepository.findById(1L)).thenReturn(Optional.of(existingNote));
        when(noteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ApplicationNoteResponse response = noteService.updateNote(recruiter, 100L, 1L,
                ApplicationNoteRequest.builder().note("New note").build());

        assertThat(response.getNote()).isEqualTo("New note");
    }

    @Test
    void updateNote_wrongOwner_throws() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        ApplicationNote otherNote = ApplicationNote.builder()
                .id(1L).recruiter(otherRecruiter).application(application).note("Other note").build();
        when(noteRepository.findById(1L)).thenReturn(Optional.of(otherNote));

        assertThatThrownBy(() -> noteService.updateNote(recruiter, 100L, 1L,
                ApplicationNoteRequest.builder().note("New").build()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void deleteNote_success() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        ApplicationNote existingNote = ApplicationNote.builder()
                .id(1L).recruiter(recruiter).application(application).note("My note").build();
        when(noteRepository.findById(1L)).thenReturn(Optional.of(existingNote));

        noteService.deleteNote(recruiter, 100L, 1L);

        verify(noteRepository).delete(existingNote);
    }

    @Test
    void deleteNote_wrongOwner_throws() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        ApplicationNote otherNote = ApplicationNote.builder()
                .id(1L).recruiter(otherRecruiter).application(application).note("Other note").build();
        when(noteRepository.findById(1L)).thenReturn(Optional.of(otherNote));

        assertThatThrownBy(() -> noteService.deleteNote(recruiter, 100L, 1L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void deleteNote_notFound_throws() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));
        when(noteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.deleteNote(recruiter, 100L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
