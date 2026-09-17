package com.jobplatform.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AIUsageService {

    private static final Logger log = LoggerFactory.getLogger(AIUsageService.class);

    private final int maxRequestsPerUserPerHour;
    private final int maxAnalysisPerDay;
    private final int requestTimeoutMs;

    private final Map<Long, HourlyUsage> hourlyUsageMap = new ConcurrentHashMap<>();
    private final Map<Long, DailyAnalysisCount> dailyAnalysisCount = new ConcurrentHashMap<>();

    public AIUsageService(
            @Value("${app.ai.limits.requests-per-user-per-hour:30}") int maxRequestsPerUserPerHour,
            @Value("${app.ai.limits.analysis-per-day:50}") int maxAnalysisPerDay,
            @Value("${app.ai.limits.request-timeout-ms:30000}") int requestTimeoutMs) {
        this.maxRequestsPerUserPerHour = maxRequestsPerUserPerHour;
        this.maxAnalysisPerDay = maxAnalysisPerDay;
        this.requestTimeoutMs = requestTimeoutMs;
    }

    public boolean canMakeRequest(Long userId) {
        HourlyUsage usage = hourlyUsageMap.computeIfAbsent(userId, k -> new HourlyUsage());
        return usage.getCount() < maxRequestsPerUserPerHour;
    }

    public boolean canPerformAnalysis(Long userId) {
        DailyAnalysisCount usage = dailyAnalysisCount.computeIfAbsent(userId, k -> new DailyAnalysisCount());
        if (!usage.isToday()) {
            usage.reset();
        }
        return usage.getCount() < maxAnalysisPerDay;
    }

    public void recordRequest(Long userId) {
        HourlyUsage usage = hourlyUsageMap.computeIfAbsent(userId, k -> new HourlyUsage());
        usage.increment();
    }

    public void recordAnalysis(Long userId) {
        DailyAnalysisCount usage = dailyAnalysisCount.computeIfAbsent(userId, k -> new DailyAnalysisCount());
        if (!usage.isToday()) {
            usage.reset();
        }
        usage.increment();
    }

    @Scheduled(fixedRate = 3_600_000)
    public void cleanupExpiredUsage() {
        long now = System.currentTimeMillis();
        hourlyUsageMap.entrySet().removeIf(entry -> {
            HourlyUsage usage = entry.getValue();
            return now - usage.getWindowStart() > 3_600_000;
        });
        dailyAnalysisCount.entrySet().removeIf(entry -> !entry.getValue().isToday());
    }

    private static class HourlyUsage {
        private final AtomicInteger count = new AtomicInteger(0);
        private final long windowStart = System.currentTimeMillis();

        int getCount() { return count.get(); }
        long getWindowStart() { return windowStart; }
        void increment() { count.incrementAndGet(); }
    }

    private static class DailyAnalysisCount {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile LocalDate date = LocalDate.now();

        int getCount() { return count.get(); }
        boolean isToday() { return date.equals(LocalDate.now()); }
        void increment() { count.incrementAndGet(); }
        void reset() { count.set(0); date = LocalDate.now(); }
    }
}
