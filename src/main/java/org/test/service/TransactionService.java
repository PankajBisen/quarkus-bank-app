package org.test.service;


import org.test.model.dto.MoneyTransferDto;
import org.test.model.entity.Transaction;

import java.util.List;

public interface TransactionService {

  String transferMoney(MoneyTransferDto transactionDto);

  List<Transaction> transaction(String accountNumberFrom);

  List<Transaction> transactionByDays(Long numberOfDays);
}
