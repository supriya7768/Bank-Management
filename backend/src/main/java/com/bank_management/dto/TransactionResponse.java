package com.bank_management.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private Long id;
    private String transactionReference;
    private String type;
    private BigDecimal amount;
    private String status;
    private Long fromAccountId;
    private Long toAccountId;
    private LocalDateTime createdAt;

    public TransactionResponse(
            Long id,
            String transactionReference,
            String type,
            BigDecimal amount,
            String status,
            Long fromAccountId,
            Long toAccountId,
            LocalDateTime createdAt) {

        this.id = id;
        this.transactionReference = transactionReference;
        this.type = type;
        this.amount = amount;
        this.status = status;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public Long getFromAccountId() {
        return fromAccountId;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}