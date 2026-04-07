package com.payflow.transfer_service.client;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
public class AccountServiceClient {

    private final WebClient webClient;

    public AccountServiceClient(@Value("${account-service.url}") String accountServiceUrl) {
        this.webClient = WebClient.builder().baseUrl(accountServiceUrl).build();
    }

    public boolean accountExists(String accountNumber) {
        try {
            webClient.get().uri("/api/accounts/number/{number}", accountNumber).retrieve().toBodilessEntity().block();
            return true;
        } catch (Exception e) {
            log.warn("Account not found or service unavailable: number = {}", accountNumber);
            return false;
        }
    }

    public Long getAccountIdByNumber(String accountNumber) {
        try {
            AccountDto response = webClient.get().uri("/api/accounts/number/{number}", accountNumber).retrieve()
                    .bodyToMono(AccountDto.class)
                    .block();

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
            webClient.patch().uri("/api/accounts/{id}/balance", accountId).bodyValue(Map.of("amount", amount)).retrieve().toBodilessEntity().block();
            log.info("Balance adjusted: accountId={} amount={}", accountId, amount);
            return true;
        }
        catch (Exception e) {
            log.error("Failed to adjust balance: accountId={}, amount={}, error={}", accountId, amount, e.getMessage());
            return false;
        }
    }
}
