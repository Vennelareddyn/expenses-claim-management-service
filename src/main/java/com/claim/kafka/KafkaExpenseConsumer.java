package com.claim.kafka;

import com.claim.model.Expense;
import com.claim.repository.ExpenseRepository;
import com.claim.workflow.ExpenseApprovalWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class KafkaExpenseConsumer {

    private final ExpenseRepository expenseRepository;
    private final WorkflowClient workflowClient;

    public KafkaExpenseConsumer(ExpenseRepository expenseRepository, WorkflowClient workflowClient) {
        this.expenseRepository = expenseRepository;
        this.workflowClient = workflowClient;
    }

    // 🎧 Listener for expense-submitted topic → Start Temporal Workflow
    @KafkaListener(topics = "${spring.kafka.topic.expense-submitted}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeExpenseSubmitted(Expense submittedExpense) {
        String workflowId = "expense-" + UUID.randomUUID();

        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue("EXPENSE_TASK_QUEUE")
                .setWorkflowId(workflowId)
                .build();

        ExpenseApprovalWorkflow workflow = workflowClient.newWorkflowStub(ExpenseApprovalWorkflow.class, options);

        WorkflowClient.start(workflow::processExpense, submittedExpense);

        System.out.println("🚀 Started Temporal Workflow for Expense ID: " + submittedExpense.getId());
    }

    // 🎧 Listener for expense-approved topic → Update status in DB
    @KafkaListener(topics = "${spring.kafka.topic.expense-approved}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeExpenseApproved(Expense approvedExpense) {
        Optional<Expense> existingExpense = expenseRepository.findById(approvedExpense.getId());

        if (existingExpense.isPresent()) {
            Expense expense = existingExpense.get();
            expense.setStatus("APPROVED");
            expenseRepository.save(expense);
            System.out.println("✅ Expense Approved: " + expense);
        } else {
            System.out.println("⚠️ Expense not found: " + approvedExpense.getId());
        }
    }
}
