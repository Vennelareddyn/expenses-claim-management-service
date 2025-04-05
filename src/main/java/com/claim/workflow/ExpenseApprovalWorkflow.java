package com.claim.workflow;

import com.claim.model.Expense;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface ExpenseApprovalWorkflow {
    @WorkflowMethod
    void processExpense(Expense expense);
}
