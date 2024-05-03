package org.test.service;

import jakarta.ws.rs.core.Response;
import org.test.model.dto.BankDto;
import org.test.model.entity.Bank;

import java.util.List;

public interface BankService {

  String addBank(BankDto bankDto);

  Bank getBankByName(Long content);

  String updateBank(BankDto bankDto, Long bankId);

  Response deleteBank(Long bankId);

  List<Bank> getAllBank();
}


