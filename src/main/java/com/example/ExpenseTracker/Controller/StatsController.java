package com.example.ExpenseTracker.Controller;

import com.example.ExpenseTracker.DTO.GraphDTO;
import com.example.ExpenseTracker.Service.Stats.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/stats")
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/chart")
    public ResponseEntity<GraphDTO> getChartDetails() {
        log.info("Fetching chart data for last 180 days");
        try {
            return ResponseEntity.ok(statsService.getChartData());
        } catch (Exception e) {
            log.error("Error fetching chart data: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/chart/{days}")
    public ResponseEntity<GraphDTO> getChartDetailsByDays(@PathVariable int days) {
        log.info("Fetching chart data for last {} days", days);
        try {
            return ResponseEntity.ok(statsService.getChartDataByDays(days));
        } catch (Exception e) {
            log.error("Error fetching chart data for {} days: {}", days, e.getMessage());
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<?> getStats() {
        log.info("Fetching statistics");
        try {
            return ResponseEntity.ok(statsService.getStats());
        } catch (Exception e) {
            log.error("Error fetching statistics: {}", e.getMessage());
            throw e;
        }
    }
}