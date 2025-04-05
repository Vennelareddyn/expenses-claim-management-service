package com.claim.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Document(collection = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {
    @Id
    private String id;
    private String employeeId;

    private String category; // OFFICE, TRAINING, EQUIPMENT
    private Double amount;
    private String currency;
    private String description;
    private String status; // PENDING, APPROVED, REJECTED
}
