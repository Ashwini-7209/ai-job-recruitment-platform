package com.jobplatform.candidate;

import com.jobplatform.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CandidateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 100)
    @Column(name = "location", length = 100)
    private String location;

    @Size(max = 100)
    @Column(name = "headline", length = 100)
    private String headline;

    @Size(max = 2000)
    @Column(name = "bio", length = 2000)
    private String bio;

    @Size(max = 100)
    @Column(name = "current_job_title", length = 100)
    private String currentJobTitle;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Size(max = 1000)
    @Column(name = "education_summary", length = 1000)
    private String educationSummary;

    @Size(max = 2000)
    @Column(name = "skills_summary", length = 2000)
    private String skillsSummary;

    @Size(max = 255)
    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Size(max = 255)
    @Column(name = "github_url", length = 255)
    private String githubUrl;

    @Size(max = 255)
    @Column(name = "portfolio_url", length = 255)
    private String portfolioUrl;

    @Size(max = 255)
    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
