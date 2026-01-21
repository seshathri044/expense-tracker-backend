package com.example.ExpenseTracker.Service.Stats;

import com.example.ExpenseTracker.DTO.GraphDTO;
import com.example.ExpenseTracker.DTO.StatsDTO;

public interface StatsService {
    // 🔥 UPDATED: All methods now require userId
    GraphDTO getChartData(Long userId);

    GraphDTO getChartDataByDays(int days, Long userId);

    StatsDTO getStats(Long userId);
}