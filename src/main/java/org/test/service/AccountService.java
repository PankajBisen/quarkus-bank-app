package org.test.service;

import org.test.model.dto.AccountDto;

import java.util.List;

public interface AccountService {

  String saveAccountNo(AccountDto accountDto);

  List<AccountDto> accountByAccNo(String content);

  List<AccountDto> getAllAccount();

  String updateAccount(AccountDto accountDto, Long accountId);

  String deleteAccount(Long accountId);

  List<AccountDto> getAllByBankId(Long bankId);

}
