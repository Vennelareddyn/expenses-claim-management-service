package com.claim.workflow.impl;

import com.claim.model.Expense;
import com.claim.workflow.ExpenseActivities;
import com.claim.workflow.ExpenseApprovalWorkflow;
import io.temporal.workflow.Workflow;
import io.temporal.activity.ActivityOptions;

import java.time.Duration;

public class ExpenseApprovalWorkflowImpl implements ExpenseApprovalWorkflow {

    private final ExpenseActivities activities;

    public ExpenseApprovalWorkflowImpl() {
        ActivityOptions options = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(10))
                .build();

        this.activities = Workflow.newActivityStub(ExpenseActivities.class, options);
    }

    @Override
    public void processExpense(Expense expense) {
        if (activities.validateBudget(expense) && activities.getManagerApproval(expense)) {
            activities.updateExpenseStatus(expense, "APPROVED");
        } else {
            activities.updateExpenseStatus(expense, "REJECTED");
        }
    }
}
