package com.jobplatform.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    private final Map<Long, AtomicInteger> dailyAnalysisCount = new ConcurrentHashMap<>();

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
        AtomicInteger count = dailyAnalysisCount.computeIfAbsent(userId, k -> new AtomicInteger(0));
        return count.get() < maxAnalysisPerDay;
    }

    public void recordRequest(Long userId) {
        HourlyUsage usage = hourlyUsageMap.computeIfAbsent(userId, k -> new HourlyUsage());
        usage.increment();
    }

    public void recordAnalysis(Long userId) {
        dailyAnalysisCount.computeIfAbsent(userId, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public void cleanupExpiredUsage() {
        long now = System.currentTimeMillis();
        hourlyUsageMap.entrySet().removeIf(entry -> {
            HourlyUsage usage = entry.getValue();
            if (now - usage.getWindowStart() > 3_600_000) {
                return true;
            }
            return false;
        });
    }

    private static class HourlyUsage {
        private final AtomicInteger count = new AtomicInteger(0);
        private final long windowStart = System.currentTimeMillis();

        int getCount() { return count.get(); }
        long getWindowStart() { return windowStart; }
        void increment() { count.incrementAndGet(); }
    }
}
