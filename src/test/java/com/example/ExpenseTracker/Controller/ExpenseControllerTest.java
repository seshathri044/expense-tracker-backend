package com.example.ExpenseTracker.Controller;

import com.example.ExpenseTracker.Entity.Expense;
import com.example.ExpenseTracker.Repository.UserRepository;
import com.example.ExpenseTracker.Service.ExpenseService;
import com.example.ExpenseTracker.Util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ✅ PART 5a: ExpenseController Integration Tests
// 🔥 Uses MockMvc - Tests full HTTP request/response cycle
// 🔥 Uses H2 in-memory DB + application-test.properties
// 📁 File location: src/test/java/com/example/ExpenseTracker/Controller/ExpenseControllerTest.java

@SpringBootTest                  // ✅ Loads full Spring context
@AutoConfigureMockMvc            // ✅ Auto-configures MockMvc
@ActiveProfiles("test")          // ✅ Uses application-test.properties
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;     // ✅ Simulates HTTP requests

    @Autowired
    private JwtUtil jwtUtil;     // ✅ Real JwtUtil to generate test tokens

    @MockitoBean
    private ExpenseService expenseService;  // ✅ Mock service - no real DB logic

    @MockitoBean
    private UserRepository userRepository;  // ✅ Mock repo - no real DB

    private ObjectMapper objectMapper;
    private String validJwtToken;
    private Expense sampleExpense;

    private final Long TEST_USER_ID = 1L;
    private final Long EXPENSE_ID = 100L;
    private final String TEST_EMAIL = "seshathri@test.com";

    @BeforeEach
    void setUp() {
        // ✅ ObjectMapper with Java 8 date support
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // ✅ Generate a real JWT token for authenticated requests
        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username(TEST_EMAIL)
                        .password("encodedPassword")
                        .authorities(Collections.emptyList())
                        .build();
        validJwtToken = jwtUtil.generateToken(userDetails, TEST_USER_ID, "Seshathri");

        // ✅ Mock: UserRepository returns user for JWT filter
        com.example.ExpenseTracker.Entity.UserEntity mockUser =
                com.example.ExpenseTracker.Entity.UserEntity.builder()
                        .id(TEST_USER_ID)
                        .userId("uuid-1234")
                        .name("Seshathri")
                        .email(TEST_EMAIL)
                        .password("encodedPassword")
                        .isAccountVerified(true)
                        .verifyOtpExpireAt(0L)
                        .resetOtpExpiresAt(0L)
                        .build();
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockUser));

        // ✅ Build sample expense
        sampleExpense = new Expense();
        sampleExpense.setId(EXPENSE_ID);
        sampleExpense.setUserId(TEST_USER_ID);
        sampleExpense.setAmount(500.0);
        sampleExpense.setCategory("Food");
        sampleExpense.setDescription("Lunch");
        sampleExpense.setDate(LocalDate.of(2024, 1, 15));
        sampleExpense.setNotes("Office lunch");
    }

    // =========================================================
    // ✅ TEST GROUP 1: POST /expense - Create expense
    // =========================================================

    @Test
    @DisplayName("✅ POST /expense - Should create expense and return 201")
    void postExpense_Returns201_WhenValidRequest() throws Exception {
        // ARRANGE
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("amount", 500.0);
        requestBody.put("category", "Food");
        requestBody.put("description", "Lunch");
        requestBody.put("date", "2024-01-15");
        requestBody.put("notes", "Office lunch");

        when(expenseService.postExpense(any(), eq(TEST_USER_ID))).thenReturn(sampleExpense);

        // ACT + ASSERT
        mockMvc.perform(post("/expense")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Expense added successfully"));
    }

    @Test
    @DisplayName("❌ POST /expense - Should return 401 when no JWT token")
    void postExpense_Returns401_WhenNoToken() throws Exception {
        // ARRANGE
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("amount", 500.0);
        requestBody.put("category", "Food");
        requestBody.put("description", "Lunch");
        requestBody.put("date", "2024-01-15");

        // ACT + ASSERT: No Authorization header → 401
        mockMvc.perform(post("/expense")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("❌ POST /expense - Should return 400 when amount is missing")
    void postExpense_Returns400_WhenAmountMissing() throws Exception {
        // ARRANGE: No amount in body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("category", "Food");
        requestBody.put("description", "Lunch");
        requestBody.put("date", "2024-01-15");

        // ACT + ASSERT
        mockMvc.perform(post("/expense")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("❌ POST /expense - Should return 400 when category is missing")
    void postExpense_Returns400_WhenCategoryMissing() throws Exception {
        // ARRANGE: No category
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("amount", 500.0);
        requestBody.put("description", "Lunch");
        requestBody.put("date", "2024-01-15");

        // ACT + ASSERT
        mockMvc.perform(post("/expense")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // =========================================================
    // ✅ TEST GROUP 2: GET /expense/all - Get all expenses
    // =========================================================

    @Test
    @DisplayName("✅ GET /expense/all - Should return expenses list with 200")
    void getAllExpenses_Returns200_WithExpensesList() throws Exception {
        // ARRANGE
        Expense expense2 = new Expense();
        expense2.setId(101L);
        expense2.setUserId(TEST_USER_ID);
        expense2.setAmount(300.0);
        expense2.setCategory("Transport");
        expense2.setDescription("Bus pass");
        expense2.setDate(LocalDate.of(2024, 1, 20));

        when(expenseService.getAllExpenses(TEST_USER_ID))
                .thenReturn(Arrays.asList(sampleExpense, expense2));

        // ACT + ASSERT
        mockMvc.perform(get("/expense/all")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("✅ GET /expense/all - Should return empty list when no expenses")
    void getAllExpenses_Returns200_WithEmptyList() throws Exception {
        // ARRANGE
        when(expenseService.getAllExpenses(TEST_USER_ID)).thenReturn(Collections.emptyList());

        // ACT + ASSERT
        mockMvc.perform(get("/expense/all")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("❌ GET /expense/all - Should return 401 when no token")
    void getAllExpenses_Returns401_WhenNoToken() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(get("/expense/all"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // ✅ TEST GROUP 3: GET /expense/{id} - Get by ID
    // =========================================================

    @Test
    @DisplayName("✅ GET /expense/{id} - Should return expense with 200")
    void getExpenseById_Returns200_WhenFound() throws Exception {
        // ARRANGE
        when(expenseService.getExpenseById(EXPENSE_ID, TEST_USER_ID)).thenReturn(sampleExpense);

        // ACT + ASSERT
        mockMvc.perform(get("/expense/" + EXPENSE_ID)
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(EXPENSE_ID))
                .andExpect(jsonPath("$.data.amount").value(500.0))
                .andExpect(jsonPath("$.data.category").value("Food"));
    }

    @Test
    @DisplayName("❌ GET /expense/{id} - Should return 404 when expense not found")
    void getExpenseById_Returns404_WhenNotFound() throws Exception {
        // ARRANGE
        when(expenseService.getExpenseById(EXPENSE_ID, TEST_USER_ID))
                .thenThrow(new EntityNotFoundException("Expense not found with id: " + EXPENSE_ID));

        // ACT + ASSERT
        mockMvc.perform(get("/expense/" + EXPENSE_ID)
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("❌ GET /expense/{id} - Should return 401 when no token")
    void getExpenseById_Returns401_WhenNoToken() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(get("/expense/" + EXPENSE_ID))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // ✅ TEST GROUP 4: PUT /expense/{id} - Update expense
    // =========================================================

    @Test
    @DisplayName("✅ PUT /expense/{id} - Should update and return 200")
    void updateExpense_Returns200_WhenSuccess() throws Exception {
        // ARRANGE
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("amount", 750.0);
        requestBody.put("category", "Transport");
        requestBody.put("description", "Updated");
        requestBody.put("date", "2024-02-01");

        Expense updatedExpense = new Expense();
        updatedExpense.setId(EXPENSE_ID);
        updatedExpense.setUserId(TEST_USER_ID);
        updatedExpense.setAmount(750.0);
        updatedExpense.setCategory("Transport");
        updatedExpense.setDescription("Updated");
        updatedExpense.setDate(LocalDate.of(2024, 2, 1));

        when(expenseService.updateExpense(eq(EXPENSE_ID), any(), eq(TEST_USER_ID)))
                .thenReturn(updatedExpense);

        // ACT + ASSERT
        mockMvc.perform(put("/expense/" + EXPENSE_ID)
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Expense updated successfully"));
    }

    @Test
    @DisplayName("❌ PUT /expense/{id} - Should return 404 when expense not found")
    void updateExpense_Returns404_WhenNotFound() throws Exception {
        // ARRANGE
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("amount", 750.0);
        requestBody.put("category", "Transport");
        requestBody.put("description", "Updated");
        requestBody.put("date", "2024-02-01");

        when(expenseService.updateExpense(eq(EXPENSE_ID), any(), eq(TEST_USER_ID)))
                .thenThrow(new EntityNotFoundException("Expense not found with id: " + EXPENSE_ID));

        // ACT + ASSERT
        mockMvc.perform(put("/expense/" + EXPENSE_ID)
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("❌ PUT /expense/{id} - Should return 401 when no token")
    void updateExpense_Returns401_WhenNoToken() throws Exception {
        // ARRANGE
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("amount", 750.0);
        requestBody.put("category", "Transport");
        requestBody.put("description", "Updated");
        requestBody.put("date", "2024-02-01");

        // ACT + ASSERT
        mockMvc.perform(put("/expense/" + EXPENSE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // ✅ TEST GROUP 5: DELETE /expense/{id} - Delete expense
    // =========================================================

    @Test
    @DisplayName("✅ DELETE /expense/{id} - Should delete and return 200")
    void deleteExpense_Returns200_WhenSuccess() throws Exception {
        // ARRANGE
        doNothing().when(expenseService).deleteExpense(EXPENSE_ID, TEST_USER_ID);

        // ACT + ASSERT
        mockMvc.perform(delete("/expense/" + EXPENSE_ID)
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Expense deleted successfully"));
    }

    @Test
    @DisplayName("❌ DELETE /expense/{id} - Should return 404 when expense not found")
    void deleteExpense_Returns404_WhenNotFound() throws Exception {
        // ARRANGE
        doThrow(new EntityNotFoundException("Expense not found with id: " + EXPENSE_ID))
                .when(expenseService).deleteExpense(EXPENSE_ID, TEST_USER_ID);

        // ACT + ASSERT
        mockMvc.perform(delete("/expense/" + EXPENSE_ID)
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("❌ DELETE /expense/{id} - Should return 401 when no token")
    void deleteExpense_Returns401_WhenNoToken() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(delete("/expense/" + EXPENSE_ID))
                .andExpect(status().isUnauthorized());
    }
}