package com.bank_management.service;

import com.bank_management.dto.AccountRequest;
import com.bank_management.dto.AccountResponse;
import com.bank_management.dto.TransactionResponse;
import com.bank_management.dto.TransferRequest;
import com.bank_management.entity.Account;
import com.bank_management.entity.Customer;
import com.bank_management.entity.Transaction;
import com.bank_management.enums.AccountStatus;
import com.bank_management.enums.AccountType;
import com.bank_management.enums.TransactionStatus;
import com.bank_management.enums.TransactionType;
import com.bank_management.exception.AccountNotFoundException;
import com.bank_management.exception.CustomerNotFoundException;
import com.bank_management.exception.DuplicateIdempotencyKeyException;
import com.bank_management.exception.InsufficientBalanceException;
import com.bank_management.repository.AccountRepository;
import com.bank_management.repository.CustomerRepository;
import com.bank_management.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferExecutionService transferExecutionService;

    public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository, TransactionRepository transactionRepository, TransferExecutionService transferExecutionService){
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.transferExecutionService = transferExecutionService;
    }

    public AccountResponse createAccount(AccountRequest request){

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id : " + request.getCustomerId()));

        Account account = new Account();
        account.setAccountType(AccountType.valueOf(request.getAccountType()));
        account.setCustomer(customer);
        account.setAccountNumber(generateAccountNumber());
        Account savedAccount = accountRepository.save(account);

        return new AccountResponse(savedAccount.getId(),
                savedAccount.getAccountNumber(),
                savedAccount.getAccountType().name(),
                savedAccount.getBalance(),
                savedAccount.getStatus().name(),
                savedAccount.getCustomer().getId());
    }

    public String generateAccountNumber(){
        long accountNumber = 10000000L + accountRepository.count()+1;
        return String.valueOf(accountNumber);
    }

    public AccountResponse getAccountById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id : " + accountId));

        return new AccountResponse(account.getId(),
                account.getAccountNumber(),
                account.getAccountType().name(),
                account.getBalance(),
                account.getStatus().name(),
                account.getCustomer().getId());
    }

    public List<AccountResponse> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return accounts.stream().map(account -> new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType().name(),
                account.getBalance(),
                account.getStatus().name(),
                account.getCustomer().getId()
        )).toList();
    }

    @Transactional
    public AccountResponse deposit(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException("Account not found with id : " + accountId));
        if(account.getStatus() != AccountStatus.ACTIVE){
        throw new RuntimeException("Account is not active. Cannot perform deposit.");
        }
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new RuntimeException("Deposit amount must be greater than zero.");
        }
        account.setBalance(account.getBalance().add(amount));
        Account savedAccount = accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setTransactionReference( "TXN-" + System.currentTimeMillis());
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setToAccount(account);
        transactionRepository.save(transaction);

        return new AccountResponse(savedAccount.getId(),
                savedAccount.getAccountNumber(),
                savedAccount.getAccountType().name(),
                savedAccount.getBalance(),
                savedAccount.getStatus().name(),
                savedAccount.getCustomer().getId());
    }

    @Transactional
    public AccountResponse withdraw(Long accountId, BigDecimal amount){
        Account account = accountRepository.findByIdForUpdate(accountId).orElseThrow(() -> new AccountNotFoundException("Account not found with id : " + accountId));
        if(account.getStatus() != AccountStatus.ACTIVE){
            throw new RuntimeException("Account is not active. Cannot perform withdrawal.");
        }
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new RuntimeException("Withdrawal amount must be greater than zero.");
        }
        if(account.getBalance().compareTo(amount) < 0){
            throw new InsufficientBalanceException("Insufficient balance for withdrawal.");
        }
        account.setBalance(account.getBalance().subtract(amount));
        Account savedAccount = accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setTransactionReference("TXN-" + System.currentTimeMillis());
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setFromAccount(account);
        transactionRepository.save(transaction);

        return new AccountResponse(savedAccount.getId(),
                savedAccount.getAccountNumber(),
                savedAccount.getAccountType().name(),
                savedAccount.getBalance(),
                savedAccount.getStatus().name(),
                savedAccount.getCustomer().getId());
    }

    public TransactionResponse transfer(TransferRequest request) {

        Transaction existingTransaction = transactionRepository.findByIdempotencyKey(request.getIdempotencyKey()).orElse(null);

        // Step 1: Handle a retry using an existing idempotency key
        if (existingTransaction != null) {
            boolean sameFromAccount = existingTransaction.getFromAccount().getId().equals(request.getFromAccountId());
            boolean sameToAccount = existingTransaction.getToAccount().getId().equals(request.getToAccountId());
            boolean sameAmount = existingTransaction.getAmount().compareTo(request.getAmount()) == 0;

            if (!sameFromAccount || !sameToAccount || !sameAmount) {
                throw new DuplicateIdempotencyKeyException("Idempotency key has already been used for a different request");
            }
            return mapToResponse(existingTransaction);
        }

        // Step 2: Execute the transfer in the separate transactional service
        try {
            return transferExecutionService.executeTransfer(request);
        } catch (DataIntegrityViolationException e) {
            // Step 3: A concurrent request may have inserted the same key.
            // Look up the transaction after the failed transaction has rolled back.
            Transaction concurrentTransaction =
                    transactionRepository.findByIdempotencyKey(request.getIdempotencyKey()).orElse(null);

            if (concurrentTransaction == null) {
                throw e;
            }

            boolean sameFromAccount = concurrentTransaction.getFromAccount().getId().equals(request.getFromAccountId());
            boolean sameToAccount = concurrentTransaction.getToAccount().getId().equals(request.getToAccountId());
            boolean sameAmount = concurrentTransaction.getAmount().compareTo(request.getAmount()) == 0;

            if (!sameFromAccount || !sameToAccount || !sameAmount) {
                throw new DuplicateIdempotencyKeyException("Idempotency key has already been used for a different request");
            }

            return mapToResponse(concurrentTransaction);
        }
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
