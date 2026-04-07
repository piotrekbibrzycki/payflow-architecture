package com.payflow.transfer_service.dto;

import com.payflow.transfer_service.model.Transfer;
import com.payflow.transfer_service.model.TransferStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponseDto {

    private Long id;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String title;
    private String currency;
    private TransferStatus status;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public static TransferResponseDto toDto(Transfer transfer) {
        return TransferResponseDto.builder()
                .id(transfer.getId())
                .fromAccount(transfer.getFromAccount())
                .toAccount(transfer.getToAccount())
                .amount(transfer.getAmount())
                .title(transfer.getTitle())
                .currency(transfer.getCurrency())
                .status(transfer.getStatus())
                .failureReason(transfer.getFailureReason())
                .createdAt(transfer.getCreatedAt())
                .completedAt(transfer.getCompletedAt())
                .build();
    }
}
