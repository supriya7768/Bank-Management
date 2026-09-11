package com.bank_management.controller;

import com.bank_management.dto.AccountRequest;
import com.bank_management.dto.AccountResponse;
import com.bank_management.service.AccountService;
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

}
