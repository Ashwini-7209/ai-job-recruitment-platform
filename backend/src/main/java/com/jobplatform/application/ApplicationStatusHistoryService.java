package com.jobplatform.application;

import com.jobplatform.application.enums.ApplicationStatus;
import com.jobplatform.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicationStatusHistoryService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationStatusHistoryService.class);

    private final ApplicationStatusHistoryRepository historyRepository;

    public ApplicationStatusHistoryService(ApplicationStatusHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Transactional
    public void recordTransition(Application application, ApplicationStatus oldStatus, ApplicationStatus newStatus, User changedBy) {
        ApplicationStatusHistory history = ApplicationStatusHistory.builder()
                .application(application)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .build();

        historyRepository.save(history);
        log.debug("Status history recorded: application={} {} -> {}", application.getId(), oldStatus, newStatus);
    }

    @Transactional(readOnly = true)
    public List<ApplicationStatusHistory> getHistory(Long applicationId) {
        return historyRepository.findByApplicationIdOrderByChangedAtAsc(applicationId);
    }
}
