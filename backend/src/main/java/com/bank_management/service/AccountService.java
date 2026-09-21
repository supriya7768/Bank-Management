package com.bank_management.service;

import com.bank_management.dto.AccountRequest;
import com.bank_management.dto.AccountResponse;
import com.bank_management.dto.TransferRequest;
import com.bank_management.entity.Account;
import com.bank_management.entity.Customer;
import com.bank_management.enums.AccountStatus;
import com.bank_management.enums.AccountType;
import com.bank_management.exception.AccountNotFoundException;
import com.bank_management.exception.CustomerNotFoundException;
import com.bank_management.exception.InsufficientBalanceException;
import com.bank_management.repository.AccountRepository;
import com.bank_management.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository){
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
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

        return new AccountResponse(savedAccount.getId(),
                savedAccount.getAccountNumber(),
                savedAccount.getAccountType().name(),
                savedAccount.getBalance(),
                savedAccount.getStatus().name(),
                savedAccount.getCustomer().getId());
    }

    @Transactional
    public void transfer(TransferRequest request){
        Account fromAccount = accountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new AccountNotFoundException("From account not found with id : " + request.getFromAccountId()));

        Account toAccount = accountRepository.findById(request.getToAccountId())
                .orElseThrow(() -> new AccountNotFoundException("To account not found with id : " + request.getToAccountId()));

        if(fromAccount.getStatus() != AccountStatus.ACTIVE || toAccount.getStatus() != AccountStatus.ACTIVE){
            throw new RuntimeException("One or both accounts are not active. Cannot perform transfer.");
        }

        if(request.getAmount().compareTo(BigDecimal.ZERO) <= 0){
            throw new RuntimeException("Transfer amount must be greater than zero.");
        }

        if(fromAccount.getBalance().compareTo(request.getAmount()) < 0){
            throw new InsufficientBalanceException("Insufficient balance for transfer.");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }
}
