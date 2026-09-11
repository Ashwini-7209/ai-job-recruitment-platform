package com.jobplatform.matching;

import com.jobplatform.candidate.CandidateProfile;
import com.jobplatform.candidate.CandidateProfileRepository;
import com.jobplatform.resume.Resume;
import com.jobplatform.resume.ResumeProfileData;
import com.jobplatform.resume.ResumeRepository;
import com.jobplatform.resume.enums.ResumeParsingStatus;
import com.jobplatform.resume.parsing.ResumeProfileDataRepository;
import com.jobplatform.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CandidateDataResolver {

    private static final Logger log = LoggerFactory.getLogger(CandidateDataResolver.class);

    private final CandidateProfileRepository candidateProfileRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeProfileDataRepository resumeProfileDataRepository;

    public CandidateDataResolver(
            CandidateProfileRepository candidateProfileRepository,
            ResumeRepository resumeRepository,
            ResumeProfileDataRepository resumeProfileDataRepository) {
        this.candidateProfileRepository = candidateProfileRepository;
        this.resumeRepository = resumeRepository;
        this.resumeProfileDataRepository = resumeProfileDataRepository;
    }

    public CandidateData resolve(User candidate) {
        List<String> allSkills = new ArrayList<>();
        Integer yearsOfExperience = null;
        String headline = null;
        String bio = null;
        String currentJobTitle = null;
        String location = null;
        String professionalSummary = null;
        boolean hasProfile = false;
        boolean hasResume = false;

        Optional<CandidateProfile> profileOpt = candidateProfileRepository.findByUser(candidate);
        if (profileOpt.isPresent()) {
            CandidateProfile profile = profileOpt.get();
            hasProfile = true;
            headline = profile.getHeadline();
            bio = profile.getBio();
            currentJobTitle = profile.getCurrentJobTitle();
            location = profile.getLocation();
            yearsOfExperience = profile.getYearsOfExperience();
            allSkills.addAll(SkillNormalizer.normalizeAll(profile.getSkillsSummary()));
        }

        List<Resume> activeResumes = resumeRepository.findByCandidateAndActiveTrue(candidate);
        if (!activeResumes.isEmpty()) {
            Resume activeResume = activeResumes.get(0);
            if (activeResume.getParsingStatus() == ResumeParsingStatus.COMPLETED) {
                Optional<ResumeProfileData> profileDataOpt =
                        resumeProfileDataRepository.findByResume(activeResume);
                if (profileDataOpt.isPresent()) {
                    ResumeProfileData resumeData = profileDataOpt.get();
                    hasResume = true;

                    List<String> resumeSkills = new ArrayList<>(SkillNormalizer.normalizeAll(resumeData.getSkills()));

                    if (resumeData.getTotalYearsOfExperience() != null && yearsOfExperience == null) {
                        yearsOfExperience = resumeData.getTotalYearsOfExperience();
                    }

                    List<String> techFromExperience = new ArrayList<>();
                    if (resumeData.getExperiences() != null) {
                        for (var exp : resumeData.getExperiences()) {
                            if (exp.getTechnologies() != null) {
                                techFromExperience.addAll(SkillNormalizer.normalizeAll(exp.getTechnologies()));
                            }
                        }
                    }

                    List<String> techFromProjects = new ArrayList<>();
                    if (resumeData.getProjects() != null) {
                        for (var proj : resumeData.getProjects()) {
                            if (proj.getTechnologies() != null) {
                                techFromProjects.addAll(SkillNormalizer.normalizeAll(proj.getTechnologies()));
                            }
                        }
                    }

                    if (resumeData.getProfessionalSummary() != null) {
                        professionalSummary = resumeData.getProfessionalSummary();
                    }

                    if (allSkills.isEmpty()) {
                        allSkills.addAll(resumeSkills);
                    } else {
                        allSkills.addAll(resumeSkills);
                    }
                }
            }
        }

        return CandidateData.builder()
                .candidateId(candidate.getId())
                .headline(headline)
                .bio(bio)
                .currentJobTitle(currentJobTitle)
                .location(location)
                .skills(allSkills)
                .yearsOfExperience(yearsOfExperience)
                .professionalSummary(professionalSummary)
                .resumeDataAvailable(hasResume)
                .profileDataAvailable(hasProfile)
                .build();
    }
}
