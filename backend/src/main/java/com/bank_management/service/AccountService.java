package com.bank_management.service;

import com.bank_management.dto.AccountRequest;
import com.bank_management.dto.AccountResponse;
import com.bank_management.entity.Account;
import com.bank_management.entity.Customer;
import com.bank_management.enums.AccountType;
import com.bank_management.exception.AccountNotFoundException;
import com.bank_management.exception.CustomerNotFoundException;
import com.bank_management.repository.AccountRepository;
import com.bank_management.repository.CustomerRepository;
import org.springframework.stereotype.Service;

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
}
