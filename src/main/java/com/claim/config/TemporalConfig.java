package com.claim.config;

import com.claim.workflow.ExpenseApprovalWorkflow;
import com.claim.workflow.impl.ExpenseApprovalWorkflowImpl;
import com.claim.workflow.ExpenseActivities;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.Worker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalConfig {

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        return WorkflowServiceStubs.newInstance();
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs service) {
        return WorkflowClient.newInstance(service);
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient client, ExpenseActivities expenseActivities) {
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker("EXPENSE_TASK_QUEUE");

        worker.registerWorkflowImplementationTypes(ExpenseApprovalWorkflowImpl.class);
        worker.registerActivitiesImplementations(expenseActivities);

        factory.start();
        return factory;
    }
}
