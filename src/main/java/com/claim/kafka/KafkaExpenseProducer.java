package com.claim.kafka;

import com.claim.model.Expense;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaExpenseProducer {

    private final KafkaTemplate<String, Expense> kafkaTemplate;

    public KafkaExpenseProducer(KafkaTemplate<String, Expense> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishExpenseSubmitted(Expense expense) {
        kafkaTemplate.send("expense-submitted", expense);
        System.out.println("📤 Expense Submitted: " + expense);
    }
    public void publishExpenseApproved(Expense expense) {
        kafkaTemplate.send("expense-approved", expense.getId(), expense);
    }
}
