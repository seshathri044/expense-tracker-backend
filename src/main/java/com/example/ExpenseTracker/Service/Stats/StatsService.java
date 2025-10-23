package com.example.ExpenseTracker.Service.Stats;

import com.example.ExpenseTracker.DTO.GraphDTO;
import com.example.ExpenseTracker.DTO.StatsDTO;

public interface StatsService {
    GraphDTO getChartData();

    GraphDTO getChartDataByDays(int days);

    StatsDTO getStats();
}
