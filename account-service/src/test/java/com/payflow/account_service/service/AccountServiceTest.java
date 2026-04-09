package com.payflow.account_service.service;

import com.payflow.account_service.dto.AccountResponseDto;
import com.payflow.account_service.model.Account;
import com.payflow.account_service.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void shouldReturnAccountById() {
        Long accountId = 1L;

        Account account = Account.builder()
                .id(accountId)
                .accountNumber("ACC-001")
                .ownerName("Test User")
                .balance(new BigDecimal("1000.00"))
                .currency("PLN")
                .createdAt(LocalDateTime.now())
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        AccountResponseDto result = accountService.getAccountById(accountId);

        assertEquals(accountId, result.getId());
        assertEquals("ACC-001", result.getAccountNumber());
        assertEquals("Test User", result.getOwnerName());
        assertEquals(new BigDecimal("1000.00"), result.getBalance());
        assertEquals("PLN", result.getCurrency());
    }
}