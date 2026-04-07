package com.payflow.transfer_service.repository;

import com.payflow.transfer_service.model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findByFromAccountOrToAccount(String fromAccount, String toAccount);
}
