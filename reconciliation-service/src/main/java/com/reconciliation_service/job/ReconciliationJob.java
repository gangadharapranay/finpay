package com.reconciliation_service.job;

import com.reconciliation_service.service.ReconciliationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ReconciliationJob {

    private final ReconciliationService reconciliationService;

    public ReconciliationJob(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @Scheduled(fixedRate = 60000) // every 1 min (for testing)
    public void run() {
        reconciliationService.execute(LocalDate.now());
    }
}

