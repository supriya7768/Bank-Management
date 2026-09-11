package com.bank_management.dto;

public class AccountRequest {

    private Long customerId;

    private String accountType;

    public AccountRequest() {
    }

    public AccountRequest(Long customerId, String accountType) {
        this.customerId = customerId;
        this.accountType = accountType;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}
