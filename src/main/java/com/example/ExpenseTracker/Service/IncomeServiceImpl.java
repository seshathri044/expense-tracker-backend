package com.example.ExpenseTracker.Service;

import com.example.ExpenseTracker.DTO.IncomeDTO;
import com.example.ExpenseTracker.Entity.Income;
import com.example.ExpenseTracker.Repository.IncomeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;

    @Override
    public Income postIncome(IncomeDTO incomeDTO, Long userId) {
        log.info("Creating income for userId: {} with amount: {}", userId, incomeDTO.getAmount());
        Income income = new Income();
        income.setUserId(userId); // 🔥 CRITICAL: Set userId
        return saveOrUpdateIncome(income, incomeDTO);
    }

    private Income saveOrUpdateIncome(Income income, IncomeDTO incomeDTO) {
        income.setTitle(incomeDTO.getTitle());
        income.setDate(incomeDTO.getDate());
        income.setAmount(incomeDTO.getAmount());
        income.setCategory(incomeDTO.getCategory());
        income.setDescription(incomeDTO.getDescription());

        Income savedIncome = incomeRepository.save(income);
        log.info("Income saved with ID: {} for userId: {}", savedIncome.getId(), savedIncome.getUserId());
        return savedIncome;
    }

    @Override
    public Income updateIncome(Long id, IncomeDTO incomeDTO, Long userId) {
        // 🔥 FIXED: Verify user owns this income before updating
        Optional<Income> optionalIncome = incomeRepository.findByIdAndUserId(id, userId);
        if (optionalIncome.isPresent()) {
            return saveOrUpdateIncome(optionalIncome.get(), incomeDTO);
        } else {
            throw new EntityNotFoundException("Income not found with id: " + id + " for user: " + userId);
        }
    }

    @Override
    public List<IncomeDTO> getAllIncomes(Long userId) {
        // 🔥 FIXED: Only fetch user's income
        List<IncomeDTO> incomes = incomeRepository.findByUserId(userId).stream()
                .sorted(Comparator.comparing(Income::getDate).reversed())
                .map(Income::getIncomeDTO)
                .collect(Collectors.toList());
        log.info("Fetched {} incomes for userId: {}", incomes.size(), userId);
        return incomes;
    }

    @Override
    public IncomeDTO getIncomeById(Long id, Long userId) {
        // 🔥 FIXED: Verify user owns this income
        Optional<Income> optionalIncome = incomeRepository.findByIdAndUserId(id, userId);
        if (optionalIncome.isPresent()) {
            return optionalIncome.get().getIncomeDTO();
        } else {
            throw new EntityNotFoundException("Income not found with id: " + id + " for user: " + userId);
        }
    }

    @Override
    public void deleteIncome(Long id, Long userId) {
        // 🔥 FIXED: Verify user owns this income before deleting
        Optional<Income> optionalIncome = incomeRepository.findByIdAndUserId(id, userId);
        if (optionalIncome.isPresent()) {
            incomeRepository.deleteById(id);
            log.info("Income deleted with id: {} by userId: {}", id, userId);
        } else {
            throw new EntityNotFoundException("Income not found with id: " + id + " for user: " + userId);
        }
    }
}