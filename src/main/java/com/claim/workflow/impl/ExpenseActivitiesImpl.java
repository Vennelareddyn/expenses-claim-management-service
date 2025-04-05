package com.claim.workflow.impl;

import com.claim.kafka.KafkaExpenseProducer;
import com.claim.model.Expense;
import com.claim.repository.ExpenseRepository;
import com.claim.workflow.ExpenseActivities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ExpenseActivitiesImpl implements ExpenseActivities {
    @Autowired
    private MongoTemplate mongoTemplate;
    private final ExpenseRepository expenseRepository;
    private final KafkaExpenseProducer kafkaExpenseProducer;

    public ExpenseActivitiesImpl(ExpenseRepository expenseRepository, KafkaExpenseProducer kafkaExpenseProducer) {
        this.expenseRepository = expenseRepository;
        this.kafkaExpenseProducer = kafkaExpenseProducer;
    }

    @Override
    public boolean validateBudget(Expense expense) {
        // Budget limits based on category
        double budgetLimit;

        switch (expense.getCategory().toUpperCase()) {
            case "OFFICE":
                budgetLimit = 5000;
                break;
            case "TRAINING":
                budgetLimit = 8000;
                break;
            case "EQUIPMENT":
                budgetLimit = 12000;
                break;
            default:
                budgetLimit = 3000;
        }

        boolean isValid = expense.getAmount() <= budgetLimit;
        System.out.println("🔍 Budget Validation: Amount = " + expense.getAmount() + ", Limit = " + budgetLimit + " => " + (isValid ? "✅ Valid" : "❌ Invalid"));
        return isValid;
    }

    @Override
    public boolean getManagerApproval(Expense expense) {
        // Simulate manual approval logic
        if (expense.getAmount() <= 2000) {
            System.out.println("🟢 Auto-approved by manager for low amount: " + expense.getAmount());
            return true;
        }

        // Simulate a 70% chance of manager approval
        boolean approved = Math.random() < 0.7;
        System.out.println("👔 Manager Approval: " + (approved ? "✅ Approved" : "❌ Rejected") + " for amount " + expense.getAmount());
        return approved;
    }

    @Override
    public void updateExpenseStatus(Expense expense, String status) {
        String category = expense.getCategory();
        String expenseId=expense.getId();
        Query query = new Query(Criteria.where("_id").is(expenseId).and("category").is(category));
        Update update = new Update().set("status", status);
        mongoTemplate.updateFirst(query, update, Expense.class);
        if(Objects.equals(status, "APPROVED")){
            kafkaExpenseProducer.publishExpenseApproved(expense);
        }

        System.out.println("📦 Expense status updated to: " + status + " for ID: " + expense.getId());
    }

}
