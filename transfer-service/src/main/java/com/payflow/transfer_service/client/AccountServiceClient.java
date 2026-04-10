package com.payflow.transfer_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;
@Slf4j
@Component
public class AccountServiceClient {

    private final RestClient restClient;

    public AccountServiceClient(@Value("${account-service.url}") String accountServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(accountServiceUrl).build();
    }

    public boolean accountExists(String accountNumber) {
        try {
            restClient.get().uri("/api/accounts/number/{number}", accountNumber).retrieve().toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.warn("Account not found or service unavailable: number = {}", accountNumber);
            return false;
        }
    }

    public Long getAccountIdByNumber(String accountNumber) {
        try {
            AccountDto response = restClient.get().uri("/api/accounts/number/{number}", accountNumber).retrieve()
                    .body(AccountDto.class);

            if (response != null) {
                return response.getId();
            }
            return null;
        } catch (Exception e) {
            log.warn("Failed to get account id: number = {}, error={}", accountNumber, e.getMessage());
            return null;
        }
    }

    public boolean adjustBalance(Long accountId, BigDecimal amount) {
        try {
            restClient.patch().uri("/api/accounts/{id}/balance", accountId).body(Map.of("amount", amount)).retrieve().toBodilessEntity();
            log.info("Balance adjusted: accountId={} amount={}", accountId, amount);
            return true;
        }
        catch (Exception e) {
            log.error("Failed to adjust balance: accountId={}, amount={}, error={}", accountId, amount, e.getMessage());
            return false;
        }
    }
}
