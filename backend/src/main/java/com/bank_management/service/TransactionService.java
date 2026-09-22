package com.bank_management.service;

import com.bank_management.dto.TransactionResponse;
import com.bank_management.entity.Transaction;
import com.bank_management.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Page<TransactionResponse> getTransactionsByAccount(Long accountId, Pageable pageable) {

        Page<Transaction> transactions = transactionRepository.findByFromAccountIdOrToAccountId(accountId,accountId,  pageable);

        return transactions
                .map(transaction -> new TransactionResponse(
                        transaction.getId(),
                        transaction.getTransactionReference(),
                        transaction.getType().name(),
                        transaction.getAmount(),
                        transaction.getStatus().name(),
                        transaction.getFromAccount() != null ? transaction.getFromAccount().getId() : null,
                        transaction.getToAccount() != null ? transaction.getToAccount().getId() : null,
                        transaction.getCreatedAt()));

    }
}