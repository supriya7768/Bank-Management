package com.bank_management.controller;

import com.bank_management.dto.AccountRequest;
import com.bank_management.dto.AccountResponse;
import com.bank_management.dto.DepositRequest;
import com.bank_management.dto.TransferRequest;
import com.bank_management.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService){
        this.accountService=accountService;
    }

    @PostMapping
    public AccountResponse createAccount(@RequestBody AccountRequest request){
        return accountService.createAccount(request);
    }

    @GetMapping("/{id}")
    public AccountResponse getAccountById(@PathVariable("id") Long accountId) {
        return accountService.getAccountById(accountId);
    }

    @GetMapping
    public List<AccountResponse> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @PostMapping("/{id}/deposit")
    public AccountResponse deposit(@PathVariable("id") Long accountId, @Valid @RequestBody DepositRequest request) {
        return accountService.deposit(accountId, request.getAmount());
    }

    @PostMapping("/{id}/withdraw")
    public AccountResponse withdraw(@PathVariable("id") Long accountId, @Valid @RequestBody DepositRequest request) {
        return accountService.withdraw(accountId, request.getAmount());
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request) {
        accountService.transfer(request);
        return ResponseEntity.ok().build();
    }

}
