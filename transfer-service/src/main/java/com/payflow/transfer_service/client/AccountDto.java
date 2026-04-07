package com.payflow.transfer_service.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AccountDto {
    private Long id;
    private String accountNumber;
    private String ownerName;
    private BigDecimal balance;
    private String currency;

}
