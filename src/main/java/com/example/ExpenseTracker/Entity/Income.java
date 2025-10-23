package com.example.ExpenseTracker.Entity;

import com.example.ExpenseTracker.DTO.IncomeDTO;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
@Entity
@Table(name = "income")
@Data
public class Income {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String title;

        private Integer amount;

        private LocalDate date;

        private String category;

        private String description;

        public IncomeDTO getIncomeDTO(){
        IncomeDTO incomeDTO = new IncomeDTO();
        incomeDTO.setId(id);
        incomeDTO.setTitle(title);
        incomeDTO.setAmount(amount);
        incomeDTO.setCategory(category);
        incomeDTO.setDescription(description);
        incomeDTO.setDate(date);

        return incomeDTO;
    }

}
