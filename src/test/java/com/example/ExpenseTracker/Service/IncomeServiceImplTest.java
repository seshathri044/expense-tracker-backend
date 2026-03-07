package com.example.ExpenseTracker.Service;

import com.example.ExpenseTracker.DTO.IncomeDTO;
import com.example.ExpenseTracker.Entity.Income;
import com.example.ExpenseTracker.Repository.IncomeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// ✅ PART 2: IncomeService Unit Tests
// 🔥 Pure Mockito - NO real database, NO Spring context needed
// 📁 File location: src/test/java/com/example/ExpenseTracker/Service/IncomeServiceImplTest.java

@ExtendWith(MockitoExtension.class)
class IncomeServiceImplTest {

    @Mock
    private IncomeRepository incomeRepository;

    @InjectMocks
    private IncomeServiceImpl incomeService;

    // ✅ Test data constants
    private Income sampleIncome;
    private IncomeDTO sampleIncomeDTO;
    private final Long USER_ID = 1L;
    private final Long INCOME_ID = 100L;

    @BeforeEach
    void setUp() {
        // ✅ Build sample Income entity (what DB returns)
        sampleIncome = new Income();
        sampleIncome.setId(INCOME_ID);
        sampleIncome.setUserId(USER_ID);
        sampleIncome.setTitle("Monthly Salary");
        sampleIncome.setAmount(50000.0);
        sampleIncome.setCategory("Salary");
        sampleIncome.setDescription("January salary from company");
        sampleIncome.setDate(LocalDate.of(2024, 1, 1));

        // ✅ Build sample IncomeDTO (what controller sends)
        sampleIncomeDTO = new IncomeDTO();
        sampleIncomeDTO.setTitle("Monthly Salary");
        sampleIncomeDTO.setAmount(50000.0);
        sampleIncomeDTO.setCategory("Salary");
        sampleIncomeDTO.setDescription("January salary from company");
        sampleIncomeDTO.setDate(LocalDate.of(2024, 1, 1));
    }

    // =========================================================
    // ✅ TEST GROUP 1: postIncome() - Create new income
    // =========================================================

    @Test
    @DisplayName("✅ Should create income successfully for valid userId")
    void postIncome_Success() {
        // ARRANGE
        when(incomeRepository.save(any(Income.class))).thenReturn(sampleIncome);

        // ACT
        Income result = incomeService.postIncome(sampleIncomeDTO, USER_ID);

        // ASSERT
        assertNotNull(result, "Result should not be null");
        assertEquals(USER_ID, result.getUserId(), "UserId should match");
        assertEquals("Monthly Salary", result.getTitle(), "Title should match");
        assertEquals(50000.0, result.getAmount(), "Amount should be 50000");
        assertEquals("Salary", result.getCategory(), "Category should match");

        // VERIFY: save was called once
        verify(incomeRepository, times(1)).save(any(Income.class));
    }

    @Test
    @DisplayName("✅ Should set userId correctly when creating income")
    void postIncome_SetsUserIdCorrectly() {
        // ARRANGE: Different user
        Long differentUserId = 777L;
        Income incomeForDifferentUser = new Income();
        incomeForDifferentUser.setUserId(differentUserId);
        incomeForDifferentUser.setAmount(30000.0);
        incomeForDifferentUser.setTitle("Freelance");

        when(incomeRepository.save(any(Income.class))).thenReturn(incomeForDifferentUser);

        // ACT
        Income result = incomeService.postIncome(sampleIncomeDTO, differentUserId);

        // ASSERT
        assertEquals(differentUserId, result.getUserId(), "UserId should be 777");
        verify(incomeRepository, times(1)).save(any(Income.class));
    }

    @Test
    @DisplayName("✅ Should create income with all fields set correctly")
    void postIncome_VerifyAllFields() {
        // ARRANGE
        when(incomeRepository.save(any(Income.class))).thenReturn(sampleIncome);

        // ACT
        Income result = incomeService.postIncome(sampleIncomeDTO, USER_ID);

        // ASSERT: Every field must match
        assertEquals("Monthly Salary", result.getTitle());
        assertEquals(50000.0, result.getAmount());
        assertEquals("Salary", result.getCategory());
        assertEquals("January salary from company", result.getDescription());
        assertEquals(LocalDate.of(2024, 1, 1), result.getDate());
    }

    @Test
    @DisplayName("✅ Should throw exception when repository returns null")
    void postIncome_ReturnsNull_WhenSaveFails() {
        // ARRANGE: Simulate repository returning null
        when(incomeRepository.save(any(Income.class))).thenReturn(null);

        // ACT & ASSERT: NPE thrown because savedIncome is null
        assertThrows(NullPointerException.class, () -> {
            incomeService.postIncome(sampleIncomeDTO, USER_ID);
        });
    }

    // =========================================================
    // ✅ TEST GROUP 2: getAllIncomes() - Fetch all incomes
    // =========================================================

    @Test
    @DisplayName("✅ Should return all incomes as DTO list for a userId")
    void getAllIncomes_ReturnsListForUser() {
        // ARRANGE: Create 2 incomes for the same user
        Income income1 = new Income();
        income1.setId(1L);
        income1.setUserId(USER_ID);
        income1.setTitle("Salary");
        income1.setAmount(50000.0);
        income1.setCategory("Salary");
        income1.setDescription("Main salary");
        income1.setDate(LocalDate.of(2024, 1, 10));

        Income income2 = new Income();
        income2.setId(2L);
        income2.setUserId(USER_ID);
        income2.setTitle("Freelance");
        income2.setAmount(15000.0);
        income2.setCategory("Freelance");
        income2.setDescription("Side project");
        income2.setDate(LocalDate.of(2024, 1, 20));

        when(incomeRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(income1, income2));

        // ACT
        List<IncomeDTO> result = incomeService.getAllIncomes(USER_ID);

        // ASSERT
        assertNotNull(result);
        assertEquals(2, result.size(), "Should return 2 incomes");

        // ✅ Service sorts by date DESCENDING - newest first
        assertEquals(15000.0, result.get(0).getAmount(), "Newest income first");
        assertEquals(50000.0, result.get(1).getAmount(), "Oldest income second");

        verify(incomeRepository, times(1)).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("✅ Should return empty list when user has no incomes")
    void getAllIncomes_EmptyList_WhenNoIncomes() {
        // ARRANGE
        when(incomeRepository.findByUserId(USER_ID)).thenReturn(Collections.emptyList());

        // ACT
        List<IncomeDTO> result = incomeService.getAllIncomes(USER_ID);

        // ASSERT
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty list");
        verify(incomeRepository, times(1)).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("✅ Should return IncomeDTO objects (not Income entities)")
    void getAllIncomes_ReturnsDTONotEntity() {
        // ARRANGE
        Income income = new Income();
        income.setId(1L);
        income.setUserId(USER_ID);
        income.setTitle("Test Income");
        income.setAmount(10000.0);
        income.setCategory("Other");
        income.setDescription("Test");
        income.setDate(LocalDate.now());

        when(incomeRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(income));

        // ACT
        List<IncomeDTO> result = incomeService.getAllIncomes(USER_ID);

        // ASSERT: Result must be IncomeDTO type, not Income entity
        assertNotNull(result);
        assertEquals(1, result.size());
        assertInstanceOf(IncomeDTO.class, result.get(0),
                "Service should return IncomeDTO, not Income entity");
    }

    @Test
    @DisplayName("✅ Should return incomes sorted by date descending")
    void getAllIncomes_SortedByDateDescending() {
        // ARRANGE: 3 incomes in random order
        Income old = new Income();
        old.setUserId(USER_ID);
        old.setTitle("Old");
        old.setAmount(10000.0);
        old.setCategory("Salary");
        old.setDescription("Old income");
        old.setDate(LocalDate.of(2024, 1, 1));

        Income middle = new Income();
        middle.setUserId(USER_ID);
        middle.setTitle("Middle");
        middle.setAmount(20000.0);
        middle.setCategory("Salary");
        middle.setDescription("Middle income");
        middle.setDate(LocalDate.of(2024, 1, 15));

        Income latest = new Income();
        latest.setUserId(USER_ID);
        latest.setTitle("Latest");
        latest.setAmount(30000.0);
        latest.setCategory("Salary");
        latest.setDescription("Latest income");
        latest.setDate(LocalDate.of(2024, 1, 30));

        // Pass in random order to verify sorting works
        when(incomeRepository.findByUserId(USER_ID))
                .thenReturn(Arrays.asList(old, latest, middle));

        // ACT
        List<IncomeDTO> result = incomeService.getAllIncomes(USER_ID);

        // ASSERT: Must come back newest to oldest
        assertEquals(30000.0, result.get(0).getAmount(), "Latest income should be first");
        assertEquals(20000.0, result.get(1).getAmount(), "Middle income should be second");
        assertEquals(10000.0, result.get(2).getAmount(), "Oldest income should be last");
    }

    // =========================================================
    // ✅ TEST GROUP 3: getIncomeById() - Fetch single income
    // =========================================================

    @Test
    @DisplayName("✅ Should return IncomeDTO when income found for correct userId")
    void getIncomeById_Success() {
        // ARRANGE
        when(incomeRepository.findByIdAndUserId(INCOME_ID, USER_ID))
                .thenReturn(Optional.of(sampleIncome));

        // ACT
        IncomeDTO result = incomeService.getIncomeById(INCOME_ID, USER_ID);

        // ASSERT
        assertNotNull(result);
        assertEquals("Monthly Salary", result.getTitle());
        assertEquals(50000.0, result.getAmount());
        verify(incomeRepository, times(1)).findByIdAndUserId(INCOME_ID, USER_ID);
    }

    @Test
    @DisplayName("❌ Should throw EntityNotFoundException when income not found")
    void getIncomeById_ThrowsException_WhenNotFound() {
        // ARRANGE
        when(incomeRepository.findByIdAndUserId(INCOME_ID, USER_ID))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> incomeService.getIncomeById(INCOME_ID, USER_ID),
                "Should throw EntityNotFoundException"
        );

        assertTrue(exception.getMessage().contains(String.valueOf(INCOME_ID)),
                "Exception message should contain income id");
        verify(incomeRepository, times(1)).findByIdAndUserId(INCOME_ID, USER_ID);
    }

    @Test
    @DisplayName("❌ Should throw exception when accessing another user's income")
    void getIncomeById_ThrowsException_WhenWrongUser() {
        // ARRANGE: Wrong user cannot see this income
        Long wrongUserId = 999L;
        when(incomeRepository.findByIdAndUserId(INCOME_ID, wrongUserId))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(
                EntityNotFoundException.class,
                () -> incomeService.getIncomeById(INCOME_ID, wrongUserId),
                "Should throw exception for wrong user"
        );
    }

    // =========================================================
    // ✅ TEST GROUP 4: updateIncome() - Update existing income
    // =========================================================

    @Test
    @DisplayName("✅ Should update income successfully for correct userId")
    void updateIncome_Success() {
        // ARRANGE: Updated DTO
        IncomeDTO updatedDTO = new IncomeDTO();
        updatedDTO.setTitle("Updated Salary");
        updatedDTO.setAmount(60000.0);
        updatedDTO.setCategory("Salary");
        updatedDTO.setDescription("Salary after increment");
        updatedDTO.setDate(LocalDate.of(2024, 2, 1));

        // Build expected updated income
        Income updatedIncome = new Income();
        updatedIncome.setId(INCOME_ID);
        updatedIncome.setUserId(USER_ID);
        updatedIncome.setTitle("Updated Salary");
        updatedIncome.setAmount(60000.0);
        updatedIncome.setCategory("Salary");
        updatedIncome.setDescription("Salary after increment");
        updatedIncome.setDate(LocalDate.of(2024, 2, 1));

        when(incomeRepository.findByIdAndUserId(INCOME_ID, USER_ID))
                .thenReturn(Optional.of(sampleIncome));
        when(incomeRepository.save(any(Income.class))).thenReturn(updatedIncome);

        // ACT
        Income result = incomeService.updateIncome(INCOME_ID, updatedDTO, USER_ID);

        // ASSERT
        assertNotNull(result);
        assertEquals(60000.0, result.getAmount(), "Amount should be updated to 60000");
        assertEquals("Updated Salary", result.getTitle(), "Title should be updated");
        verify(incomeRepository, times(1)).findByIdAndUserId(INCOME_ID, USER_ID);
        verify(incomeRepository, times(1)).save(any(Income.class));
    }

    @Test
    @DisplayName("❌ Should throw exception when updating income of another user")
    void updateIncome_ThrowsException_WhenWrongUser() {
        // ARRANGE
        Long wrongUserId = 999L;
        when(incomeRepository.findByIdAndUserId(INCOME_ID, wrongUserId))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(
                EntityNotFoundException.class,
                () -> incomeService.updateIncome(INCOME_ID, sampleIncomeDTO, wrongUserId),
                "Should throw exception for wrong user"
        );

        // CRITICAL: save() should NEVER be called
        verify(incomeRepository, never()).save(any(Income.class));
    }

    @Test
    @DisplayName("❌ Should throw exception when updating non-existent income")
    void updateIncome_ThrowsException_WhenIncomeNotFound() {
        // ARRANGE
        Long nonExistentId = 9999L;
        when(incomeRepository.findByIdAndUserId(nonExistentId, USER_ID))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> incomeService.updateIncome(nonExistentId, sampleIncomeDTO, USER_ID)
        );

        assertTrue(exception.getMessage().contains(String.valueOf(nonExistentId)));
        verify(incomeRepository, never()).save(any(Income.class));
    }

    // =========================================================
    // ✅ TEST GROUP 5: deleteIncome() - Delete income
    // =========================================================

    @Test
    @DisplayName("✅ Should delete income successfully for correct userId")
    void deleteIncome_Success() {
        // ARRANGE
        when(incomeRepository.findByIdAndUserId(INCOME_ID, USER_ID))
                .thenReturn(Optional.of(sampleIncome));
        doNothing().when(incomeRepository).deleteById(INCOME_ID);

        // ACT
        assertDoesNotThrow(
                () -> incomeService.deleteIncome(INCOME_ID, USER_ID),
                "Delete should not throw any exception"
        );

        // VERIFY: deleteById was called with correct ID
        verify(incomeRepository, times(1)).deleteById(INCOME_ID);
        verify(incomeRepository, times(1)).findByIdAndUserId(INCOME_ID, USER_ID);
    }

    @Test
    @DisplayName("❌ Should throw exception and NOT delete when wrong userId")
    void deleteIncome_ThrowsException_WhenWrongUser() {
        // ARRANGE
        Long wrongUserId = 999L;
        when(incomeRepository.findByIdAndUserId(INCOME_ID, wrongUserId))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(
                EntityNotFoundException.class,
                () -> incomeService.deleteIncome(INCOME_ID, wrongUserId),
                "Should throw exception for wrong user"
        );

        // CRITICAL: deleteById must NEVER be called for wrong user
        verify(incomeRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("❌ Should throw exception when deleting non-existent income")
    void deleteIncome_ThrowsException_WhenIncomeNotFound() {
        // ARRANGE
        Long nonExistentId = 8888L;
        when(incomeRepository.findByIdAndUserId(nonExistentId, USER_ID))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> incomeService.deleteIncome(nonExistentId, USER_ID)
        );

        assertTrue(exception.getMessage().contains(String.valueOf(nonExistentId)));

        // CRITICAL: deleteById must NEVER be called
        verify(incomeRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("✅ Should call deleteById exactly once on successful delete")
    void deleteIncome_CallsDeleteByIdExactlyOnce() {
        // ARRANGE
        when(incomeRepository.findByIdAndUserId(INCOME_ID, USER_ID))
                .thenReturn(Optional.of(sampleIncome));
        doNothing().when(incomeRepository).deleteById(INCOME_ID);

        // ACT
        incomeService.deleteIncome(INCOME_ID, USER_ID);

        // VERIFY: exactly one call, exactly right ID
        verify(incomeRepository, times(1)).deleteById(INCOME_ID);
        verify(incomeRepository, never()).deleteById(argThat(id -> !id.equals(INCOME_ID)));
    }
}