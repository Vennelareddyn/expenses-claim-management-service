package com.claim.workflow;

import com.claim.model.Expense;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ExpenseActivities {

    @ActivityMethod
    boolean validateBudget(Expense expense);

    @ActivityMethod
    boolean getManagerApproval(Expense expense);

    @ActivityMethod
    void updateExpenseStatus(Expense expense, String status);
}
