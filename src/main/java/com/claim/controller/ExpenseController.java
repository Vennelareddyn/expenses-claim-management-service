package com.claim.controller;

import com.claim.model.Expense;
import com.claim.repository.ExpenseRepository;
import com.claim.service.ExpenseService;
import org.springframework.web.bind.annotation.*;

import com.claim.kafka.KafkaExpenseProducer;

import java.util.List;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final KafkaExpenseProducer kafkaExpenseProducer;
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseRepository expenseRepository, KafkaExpenseProducer kafkaExpenseProducer, ExpenseService expenseService) {
        this.expenseRepository = expenseRepository;
        this.kafkaExpenseProducer = kafkaExpenseProducer;
        this.expenseService = expenseService;
    }

    @PostMapping
    public Expense createExpense(@RequestBody Expense expense) {
        expense.setStatus("PENDING"); // Default status
        Expense savedExpense = expenseRepository.save(expense);

        kafkaExpenseProducer.publishExpenseSubmitted(savedExpense); // Publish event

        return savedExpense;
    }

    @GetMapping("/category/{category}")
    public List<Expense> getExpensesByCategory(@PathVariable String category) {
        return expenseService.getExpensesByCategory(category);
    }
}
