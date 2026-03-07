package com.example.ExpenseTracker.Service;

import com.example.ExpenseTracker.DTO.ExpenseDTO;
import com.example.ExpenseTracker.Entity.Expense;
import com.example.ExpenseTracker.Repository.ExpenseRepository;
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

// ✅ PART 1: ExpenseService Unit Tests
// 🔥 Uses Mockito - NO real database, NO Spring context needed
// 📁 File location: src/test/java/com/example/ExpenseTracker/Service/ExpenseServiceImplTest.java

@ExtendWith(MockitoExtension.class)  // ✅ Enables Mockito annotations
class ExpenseServiceImplTest {

    // ✅ MOCK: Fake repository - no real MySQL connection
    @Mock
    private ExpenseRepository expenseRepository;

    // ✅ INJECT: Inject mock into real service class
    @InjectMocks
    private ExpenseServiceImpl expenseService;

    // ✅ Test data - reused across tests
    private Expense sampleExpense;
    private ExpenseDTO sampleExpenseDTO;
    private final Long USER_ID = 1L;
    private final Long EXPENSE_ID = 100L;

    // ✅ Runs BEFORE each test - fresh data every time
    @BeforeEach
    void setUp() {
        // Build a sample Expense entity
        sampleExpense = new Expense();
        sampleExpense.setId(EXPENSE_ID);
        sampleExpense.setUserId(USER_ID);
        sampleExpense.setAmount(500.0);
        sampleExpense.setCategory("Food");
        sampleExpense.setDescription("Lunch at office");
        sampleExpense.setDate(LocalDate.of(2024, 1, 15));
        sampleExpense.setNotes("Monthly lunch budget");

        // Build a sample ExpenseDTO (what controller sends to service)
        sampleExpenseDTO = new ExpenseDTO();
        sampleExpenseDTO.setAmount(500.0);
        sampleExpenseDTO.setCategory("Food");
        sampleExpenseDTO.setDescription("Lunch at office");
        sampleExpenseDTO.setDate(LocalDate.of(2024, 1, 15));
        sampleExpenseDTO.setNotes("Monthly lunch budget");
    }

    // =========================================================
    // ✅ TEST GROUP 1: postExpense() - Create new expense
    // =========================================================

    @Test
    @DisplayName("✅ Should create expense successfully for valid userId")
    void postExpense_Success() {
        // ARRANGE: When repository.save() is called, return our sampleExpense
        when(expenseRepository.save(any(Expense.class))).thenReturn(sampleExpense);

        // ACT: Call the real service method
        Expense result = expenseService.postExpense(sampleExpenseDTO, USER_ID);

        // ASSERT: Verify the result
        assertNotNull(result, "Result should not be null");
        assertEquals(USER_ID, result.getUserId(), "UserId should match");
        assertEquals(500.0, result.getAmount(), "Amount should be 500.0");
        assertEquals("Food", result.getCategory(), "Category should be Food");
        assertEquals("Lunch at office", result.getDescription(), "Description should match");

        // VERIFY: repository.save() was called exactly once
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    @DisplayName("✅ Should set userId correctly when creating expense")
    void postExpense_SetsUserIdCorrectly() {
        // ARRANGE
        Long differentUserId = 999L;
        Expense expenseWithDifferentUser = new Expense();
        expenseWithDifferentUser.setUserId(differentUserId);
        expenseWithDifferentUser.setAmount(200.0);
        expenseWithDifferentUser.setCategory("Transport");

        when(expenseRepository.save(any(Expense.class))).thenReturn(expenseWithDifferentUser);

        // ACT
        Expense result = expenseService.postExpense(sampleExpenseDTO, differentUserId);

        // ASSERT: userId must be the one passed in
        assertEquals(differentUserId, result.getUserId(), "UserId should be 999");
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    @DisplayName("✅ Should create expense with correct amount and category")
    void postExpense_VerifyAllFields() {
        // ARRANGE
        when(expenseRepository.save(any(Expense.class))).thenReturn(sampleExpense);

        // ACT
        Expense result = expenseService.postExpense(sampleExpenseDTO, USER_ID);

        // ASSERT: All fields must match
        assertEquals(500.0, result.getAmount());
        assertEquals("Food", result.getCategory());
        assertEquals("Lunch at office", result.getDescription());
        assertEquals(LocalDate.of(2024, 1, 15), result.getDate());
    }

    // =========================================================
    // ✅ TEST GROUP 2: getAllExpenses() - Fetch all expenses
    // =========================================================

    @Test
    @DisplayName("✅ Should return all expenses for a userId")
    void getAllExpenses_ReturnsListForUser() {
        // ARRANGE: Create 2 expenses for the same user
        Expense expense1 = new Expense();
        expense1.setId(1L);
        expense1.setUserId(USER_ID);
        expense1.setAmount(100.0);
        expense1.setDate(LocalDate.of(2024, 1, 10));

        Expense expense2 = new Expense();
        expense2.setId(2L);
        expense2.setUserId(USER_ID);
        expense2.setAmount(200.0);
        expense2.setDate(LocalDate.of(2024, 1, 20));

        when(expenseRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(expense1, expense2));

        // ACT
        List<Expense> result = expenseService.getAllExpenses(USER_ID);

        // ASSERT
        assertNotNull(result);
        assertEquals(2, result.size(), "Should return 2 expenses");

        // ✅ IMPORTANT: Service sorts by date DESCENDING - newest first
        assertEquals(200.0, result.get(0).getAmount(), "Newest expense should be first");
        assertEquals(100.0, result.get(1).getAmount(), "Oldest expense should be last");

        verify(expenseRepository, times(1)).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("✅ Should return empty list when user has no expenses")
    void getAllExpenses_EmptyList_WhenNoExpenses() {
        // ARRANGE: Return empty list from repository
        when(expenseRepository.findByUserId(USER_ID)).thenReturn(Collections.emptyList());

        // ACT
        List<Expense> result = expenseService.getAllExpenses(USER_ID);

        // ASSERT
        assertNotNull(result, "Result should not be null even if empty");
        assertTrue(result.isEmpty(), "Result should be empty");
        verify(expenseRepository, times(1)).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("✅ Should return sorted list by date descending")
    void getAllExpenses_SortedByDateDescending() {
        // ARRANGE: 3 expenses in random date order
        Expense old = new Expense();
        old.setUserId(USER_ID);
        old.setAmount(50.0);
        old.setDate(LocalDate.of(2024, 1, 1));  // oldest

        Expense middle = new Expense();
        middle.setUserId(USER_ID);
        middle.setAmount(150.0);
        middle.setDate(LocalDate.of(2024, 1, 15)); // middle

        Expense latest = new Expense();
        latest.setUserId(USER_ID);
        latest.setAmount(300.0);
        latest.setDate(LocalDate.of(2024, 1, 30)); // newest

        // Pass in random order
        when(expenseRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(old, latest, middle));

        // ACT
        List<Expense> result = expenseService.getAllExpenses(USER_ID);

        // ASSERT: Should be sorted newest to oldest
        assertEquals(300.0, result.get(0).getAmount(), "Latest expense first");
        assertEquals(150.0, result.get(1).getAmount(), "Middle expense second");
        assertEquals(50.0, result.get(2).getAmount(), "Oldest expense last");
    }

    // =========================================================
    // ✅ TEST GROUP 3: getExpenseById() - Fetch single expense
    // =========================================================

    @Test
    @DisplayName("✅ Should return expense when found for correct userId")
    void getExpenseById_Success() {
        // ARRANGE: Repository finds the expense for this user
        when(expenseRepository.findByIdAndUserId(EXPENSE_ID, USER_ID))
                .thenReturn(Optional.of(sampleExpense));

        // ACT
        Expense result = expenseService.getExpenseById(EXPENSE_ID, USER_ID);

        // ASSERT
        assertNotNull(result);
        assertEquals(EXPENSE_ID, result.getId());
        assertEquals(USER_ID, result.getUserId());
        assertEquals(500.0, result.getAmount());
        verify(expenseRepository, times(1)).findByIdAndUserId(EXPENSE_ID, USER_ID);
    }

    @Test
    @DisplayName("❌ Should throw EntityNotFoundException when expense not found")
    void getExpenseById_ThrowsException_WhenNotFound() {
        // ARRANGE: Repository returns empty (not found OR wrong user)
        when(expenseRepository.findByIdAndUserId(EXPENSE_ID, USER_ID))
                .thenReturn(Optional.empty());

        // ACT + ASSERT: Must throw EntityNotFoundException
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> expenseService.getExpenseById(EXPENSE_ID, USER_ID),
                "Should throw EntityNotFoundException when expense not found"
        );

        // Verify exception message contains the id
        assertTrue(exception.getMessage().contains(String.valueOf(EXPENSE_ID)),
                "Exception message should contain expense id");
        verify(expenseRepository, times(1)).findByIdAndUserId(EXPENSE_ID, USER_ID);
    }

    @Test
    @DisplayName("❌ Should throw exception when userId doesn't match expense owner")
    void getExpenseById_ThrowsException_WhenWrongUser() {
        // ARRANGE: A different user trying to access someone else's expense
        Long wrongUserId = 999L;
        when(expenseRepository.findByIdAndUserId(EXPENSE_ID, wrongUserId))
                .thenReturn(Optional.empty()); // Not found for this user

        // ACT + ASSERT
        assertThrows(
                EntityNotFoundException.class,
                () -> expenseService.getExpenseById(EXPENSE_ID, wrongUserId),
                "Should throw exception when wrong user tries to access expense"
        );
    }

    // =========================================================
    // ✅ TEST GROUP 4: updateExpense() - Update existing expense
    // =========================================================

    @Test
    @DisplayName("✅ Should update expense successfully for correct userId")
    void updateExpense_Success() {
        // ARRANGE: Updated DTO with new values
        ExpenseDTO updatedDTO = new ExpenseDTO();
        updatedDTO.setAmount(750.0);
        updatedDTO.setCategory("Transport");
        updatedDTO.setDescription("Monthly bus pass");
        updatedDTO.setDate(LocalDate.of(2024, 2, 1));
        updatedDTO.setNotes("Updated note");

        // Build expected updated expense
        Expense updatedExpense = new Expense();
        updatedExpense.setId(EXPENSE_ID);
        updatedExpense.setUserId(USER_ID);
        updatedExpense.setAmount(750.0);
        updatedExpense.setCategory("Transport");
        updatedExpense.setDescription("Monthly bus pass");

        when(expenseRepository.findByIdAndUserId(EXPENSE_ID, USER_ID))
                .thenReturn(Optional.of(sampleExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(updatedExpense);

        // ACT
        Expense result = expenseService.updateExpense(EXPENSE_ID, updatedDTO, USER_ID);

        // ASSERT
        assertNotNull(result);
        assertEquals(750.0, result.getAmount(), "Amount should be updated to 750");
        assertEquals("Transport", result.getCategory(), "Category should be updated");
        verify(expenseRepository, times(1)).findByIdAndUserId(EXPENSE_ID, USER_ID);
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    @DisplayName("❌ Should throw exception when updating expense of another user")
    void updateExpense_ThrowsException_WhenWrongUser() {
        // ARRANGE: Wrong userId - cannot find the expense
        Long wrongUserId = 999L;
        when(expenseRepository.findByIdAndUserId(EXPENSE_ID, wrongUserId))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(
                EntityNotFoundException.class,
                () -> expenseService.updateExpense(EXPENSE_ID, sampleExpenseDTO, wrongUserId),
                "Should throw exception when user doesn't own the expense"
        );

        // VERIFY: save() should NEVER be called
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    @DisplayName("❌ Should throw exception when updating non-existent expense")
    void updateExpense_ThrowsException_WhenExpenseNotFound() {
        // ARRANGE: Expense ID doesn't exist at all
        Long nonExistentId = 9999L;
        when(expenseRepository.findByIdAndUserId(nonExistentId, USER_ID))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> expenseService.updateExpense(nonExistentId, sampleExpenseDTO, USER_ID)
        );

        assertTrue(exception.getMessage().contains(String.valueOf(nonExistentId)));
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    // =========================================================
    // ✅ TEST GROUP 5: deleteExpense() - Delete expense
    // =========================================================

    @Test
    @DisplayName("✅ Should delete expense successfully for correct userId")
    void deleteExpense_Success() {
        // ARRANGE
        when(expenseRepository.findByIdAndUserId(EXPENSE_ID, USER_ID))
                .thenReturn(Optional.of(sampleExpense));
        doNothing().when(expenseRepository).deleteById(EXPENSE_ID);

        // ACT
        assertDoesNotThrow(
                () -> expenseService.deleteExpense(EXPENSE_ID, USER_ID),
                "Delete should not throw any exception"
        );

        // VERIFY: deleteById was called with correct ID
        verify(expenseRepository, times(1)).deleteById(EXPENSE_ID);
        verify(expenseRepository, times(1)).findByIdAndUserId(EXPENSE_ID, USER_ID);
    }

    @Test
    @DisplayName("❌ Should throw exception and NOT delete when wrong userId")
    void deleteExpense_ThrowsException_WhenWrongUser() {
        // ARRANGE: Wrong user cannot find the expense
        Long wrongUserId = 999L;
        when(expenseRepository.findByIdAndUserId(EXPENSE_ID, wrongUserId))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(
                EntityNotFoundException.class,
                () -> expenseService.deleteExpense(EXPENSE_ID, wrongUserId),
                "Should throw exception when user doesn't own the expense"
        );

        // CRITICAL: deleteById must NEVER be called for wrong user
        verify(expenseRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("❌ Should throw exception when deleting non-existent expense")
    void deleteExpense_ThrowsException_WhenExpenseNotFound() {
        // ARRANGE
        Long nonExistentId = 8888L;
        when(expenseRepository.findByIdAndUserId(nonExistentId, USER_ID))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> expenseService.deleteExpense(nonExistentId, USER_ID)
        );

        assertTrue(exception.getMessage().contains(String.valueOf(nonExistentId)));
        verify(expenseRepository, never()).deleteById(any());
    }
}