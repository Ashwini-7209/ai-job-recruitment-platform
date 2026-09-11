package com.jobplatform.resume;

import com.jobplatform.user.User;
import com.jobplatform.user.UserRepository;
import com.jobplatform.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ResumeRepositoryTests {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User savedCandidate;

    @BeforeEach
    void setUp() {
        User candidate = User.builder()
                .fullName("Test Candidate")
                .email("candidate-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.CANDIDATE)
                .enabled(true)
                .build();
        savedCandidate = userRepository.save(candidate);
    }

    private Resume createResume(String fileName, boolean active) {
        return Resume.builder()
                .candidate(savedCandidate)
                .originalFileName(fileName)
                .storedFileName("stored-" + System.nanoTime() + ".pdf")
                .contentType("application/pdf")
                .fileSize(1024L)
                .active(active)
                .build();
    }

    @Test
    void saveAndFindById() {
        Resume resume = resumeRepository.save(createResume("resume.pdf", false));

        Optional<Resume> found = resumeRepository.findById(resume.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getOriginalFileName()).isEqualTo("resume.pdf");
    }

    @Test
    void findByCandidateOrderByCreatedAtDesc() throws InterruptedException {
        Resume r1 = resumeRepository.save(createResume("first.pdf", false));
        Thread.sleep(10);
        Resume r2 = resumeRepository.save(createResume("second.pdf", true));

        List<Resume> results = resumeRepository.findByCandidateOrderByCreatedAtDesc(savedCandidate);

        assertThat(results).hasSizeGreaterThanOrEqualTo(2);
        assertThat(results.get(0).getOriginalFileName()).isEqualTo("second.pdf");
    }

    @Test
    void findByCandidateAndActiveTrue() {
        resumeRepository.save(createResume("inactive.pdf", false));
        resumeRepository.save(createResume("active.pdf", true));

        List<Resume> active = resumeRepository.findByCandidateAndActiveTrue(savedCandidate);

        assertThat(active).hasSize(1);
        assertThat(active.get(0).getOriginalFileName()).isEqualTo("active.pdf");
    }

    @Test
    void findByIdAndCandidate() {
        Resume resume = resumeRepository.save(createResume("test.pdf", false));

        Optional<Resume> found = resumeRepository.findByIdAndCandidate(resume.getId(), savedCandidate);
        assertThat(found).isPresent();

        User otherCandidate = userRepository.save(User.builder()
                .fullName("Other").email("other-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("pass")).role(UserRole.CANDIDATE).enabled(true).build());

        Optional<Resume> notFound = resumeRepository.findByIdAndCandidate(resume.getId(), otherCandidate);
        assertThat(notFound).isEmpty();
    }

    @Test
    void findByStoredFileName() {
        Resume resume = resumeRepository.save(createResume("test.pdf", false));

        Optional<Resume> found = resumeRepository.findByStoredFileName(resume.getStoredFileName());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(resume.getId());
    }

    @Test
    void existsByCandidateAndActiveTrue() {
        assertThat(resumeRepository.existsByCandidateAndActiveTrue(savedCandidate)).isFalse();

        resumeRepository.save(createResume("active.pdf", true));
        assertThat(resumeRepository.existsByCandidateAndActiveTrue(savedCandidate)).isTrue();
    }

    @Test
    void deactivateAllByCandidate() {
        resumeRepository.save(createResume("active1.pdf", true));
        resumeRepository.save(createResume("active2.pdf", true));

        resumeRepository.deactivateAllByCandidate(savedCandidate);

        List<Resume> active = resumeRepository.findByCandidateAndActiveTrue(savedCandidate);
        assertThat(active).isEmpty();
    }

    @Test
    void countByCandidate() {
        resumeRepository.save(createResume("r1.pdf", false));
        resumeRepository.save(createResume("r2.pdf", true));

        long count = resumeRepository.countByCandidate(savedCandidate);
        assertThat(count).isEqualTo(2);
    }
}
