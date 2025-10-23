package com.example.ExpenseTracker.Service;

import com.example.ExpenseTracker.DTO.ExpenseDTO;
import com.example.ExpenseTracker.Entity.Expense;
import com.example.ExpenseTracker.Repository.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements  ExpenseService{

    private final ExpenseRepository expenseRepository;

    public Expense postExpense(ExpenseDTO expenseDTO){
        return SaveOrUpdateExpense(new Expense(),expenseDTO);
    }




    private  Expense SaveOrUpdateExpense(Expense expense, ExpenseDTO expenseDTO){
        expense.setTitle(expenseDTO.getTitle());
        expense.setDate(expenseDTO.getDate());
        expense.setAmount(expenseDTO.getAmount());
        expense.setCategory(expenseDTO.getCategory());
        expense.setDescription(expenseDTO.getDescription());

        return expenseRepository.save(expense);
    }
    @Override
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll().stream()
                .sorted(Comparator.comparing(Expense::getDate).reversed())
                .collect(Collectors.toList());
    }


    public Expense getExpenseById(Long id){
        Optional<Expense> optionalExpense=expenseRepository.findById(id);
        if(optionalExpense.isPresent()){

            return optionalExpense.get();
        }
        else {
            throw new EntityNotFoundException("Expense is not present the id:"+id);
        }
    }
    public Expense updateExpense(Long id , ExpenseDTO expenseDTO){
        Optional<Expense> optionalExpense = expenseRepository.findById(id);
        if(optionalExpense.isPresent()){
            return SaveOrUpdateExpense(optionalExpense.get(),expenseDTO);
        }
        else{
            throw new EntityNotFoundException("Expense is not present with the id:"+id);
        }
    }

    public void deleteExpense(Long id){
        Optional<Expense> optionalExpense = expenseRepository.findById(id);
        if(optionalExpense.isPresent()){
            expenseRepository.deleteById(id);

        }
        else{
            throw new EntityNotFoundException("Expense is not present with the id:"+id);
        }
    }
}
