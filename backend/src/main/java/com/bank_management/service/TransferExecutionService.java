package com.bank_management.service;

import com.bank_management.dto.TransactionResponse;
import com.bank_management.dto.TransferRequest;
import com.bank_management.entity.Account;
import com.bank_management.entity.Transaction;
import com.bank_management.enums.AccountStatus;
import com.bank_management.enums.TransactionStatus;
import com.bank_management.enums.TransactionType;
import com.bank_management.exception.AccountNotFoundException;
import com.bank_management.exception.DuplicateIdempotencyKeyException;
import com.bank_management.exception.InsufficientBalanceException;
import com.bank_management.repository.AccountRepository;
import com.bank_management.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TransferExecutionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransferExecutionService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionResponse executeTransfer(TransferRequest request) {

        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new IllegalArgumentException("Source and destination accounts must be different");
        }

        Long firstAccountId = Math.min( request.getFromAccountId(), request.getToAccountId());
        Long secondAccountId = Math.max( request.getFromAccountId(), request.getToAccountId());

        Account firstAccount = accountRepository.findByIdForUpdate(firstAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + firstAccountId));

        Account secondAccount = accountRepository.findByIdForUpdate(secondAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + secondAccountId));

        Account fromAccount = request.getFromAccountId().equals(firstAccountId) ? firstAccount : secondAccount;
        Account toAccount = request.getToAccountId().equals(firstAccountId) ? firstAccount : secondAccount;

        if (fromAccount.getStatus() != AccountStatus.ACTIVE || toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Both accounts must be active to transfer money");
        }
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in source account");
        }
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction transaction = new Transaction();
        transaction.setTransactionReference("TXN-" + System.currentTimeMillis());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setAmount(request.getAmount());
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setIdempotencyKey(request.getIdempotencyKey());
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transactionRepository.saveAndFlush(transaction);

        return mapToResponse(transaction);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionReference(),
                transaction.getType().name(),
                transaction.getAmount(),
                transaction.getStatus().name(),
                transaction.getFromAccount().getId(),
                transaction.getToAccount().getId(),
                transaction.getCreatedAt()
        );
    }
}
