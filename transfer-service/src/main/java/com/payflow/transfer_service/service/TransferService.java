package com.payflow.transfer_service.service;

import com.payflow.transfer_service.client.AccountServiceClient;
import com.payflow.transfer_service.dto.TransferRequestDto;
import com.payflow.transfer_service.dto.TransferResponseDto;
import com.payflow.transfer_service.model.Transfer;
import com.payflow.transfer_service.model.TransferStatus;
import com.payflow.transfer_service.repository.TransferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class TransferService {

    private final TransferRepository transferRepository;
    private final AccountServiceClient accountServiceClient;

    public TransferService(TransferRepository transferRepository, AccountServiceClient accountServiceClient) {
        this.transferRepository = transferRepository;
        this.accountServiceClient = accountServiceClient;
    }

    public TransferResponseDto executeTransfer(TransferRequestDto request) {
        if (!accountServiceClient.accountExists(request.getFromAccount())) {
            return failTransfer(request, "Source account not found: " + request.getFromAccount());
        }
        if (!accountServiceClient.accountExists(request.getToAccount())) {
            return failTransfer(request, "Destination account not found: " + request.getToAccount());
        }
        if (request.getFromAccount().equals(request.getToAccount())) {
            return failTransfer(request, "Cannot transfer to the same account");
        }

        Long fromId = accountServiceClient.getAccountIdByNumber(request.getFromAccount());
        Long toId = accountServiceClient.getAccountIdByNumber(request.getToAccount());

        if (fromId == null || toId == null) {
            return failTransfer(request, "Failed to resolve account IDs");
        }

        Transfer transfer = Transfer.builder().fromAccount(request.getFromAccount()).toAccount(request.getToAccount()).amount(request.getAmount())
                .title(request.getTitle()).currency(request.getCurrency()).status(TransferStatus.PENDING).build();

        transfer = transferRepository.save(transfer);
        log.info("Transfer created: id={}, {}->{}, amount={}", transfer.getId(), transfer.getFromAccount(), transfer.getToAccount(), transfer.getAmount());

        boolean debitSuccess = accountServiceClient.adjustBalance(fromId, request.getAmount().negate());
        if (!debitSuccess) {
            return failExistingTransfer(transfer, "Failed to debit source account (insufficient balance or service unavailable)");
        }
        boolean creditSuccess = accountServiceClient.adjustBalance(toId, request.getAmount());

        if (!creditSuccess) {
            log.warn("Credit failed, compensating debit: transferId={}", transfer.getId());
            boolean compensationSuccess = accountServiceClient.adjustBalance(fromId, request.getAmount());

            if (!compensationSuccess) {
                log.error("CRITICAL: COMPENSATION FAILED transferId={}, manual intervention required", transfer.getId());
            }
            return failExistingTransfer(transfer, "Failed to credit destination account, debit reversed");
        }

        transfer.setStatus(TransferStatus.COMPLETED);
        transfer.setCompletedAt(LocalDateTime.now());
        transfer = transferRepository.save(transfer);
        log.info("Transfer completed: id={}", transfer.getId());

        return TransferResponseDto.toDto(transfer);
    }

    public TransferResponseDto getTransferById(Long id) {
        Transfer transfer = transferRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("Transfer not found: " + id));
        return TransferResponseDto.toDto(transfer);
    }

    public List<TransferResponseDto> getTransfersByAccount(String accountNumber) {
        return transferRepository.findByFromAccountOrToAccount(accountNumber, accountNumber).stream().map(TransferResponseDto::toDto).toList();
    }

    private TransferResponseDto failTransfer(TransferRequestDto transferRequestDto, String reason) {
        log.warn("Transfer failed before execution: reason={}", reason);
        Transfer transfer = Transfer.builder().fromAccount(transferRequestDto.getFromAccount()).toAccount(transferRequestDto.getToAccount())
                .amount(transferRequestDto.getAmount()).title(transferRequestDto.getTitle())
                .currency(transferRequestDto.getCurrency()).status(TransferStatus.FAILED).failureReason(reason)
                .build();
        transfer = transferRepository.save(transfer);
        return TransferResponseDto.toDto(transfer);
    }

    private TransferResponseDto failExistingTransfer(Transfer transfer, String reason) {
        log.warn("Transfer failed during execution: id={}, reason={}", transfer.getId(), reason);
        transfer.setStatus(TransferStatus.FAILED);
        transfer.setFailureReason(reason);
        transfer = transferRepository.save(transfer);
        return TransferResponseDto.toDto(transfer);

    }
}
