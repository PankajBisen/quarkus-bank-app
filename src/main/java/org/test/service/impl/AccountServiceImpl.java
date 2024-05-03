package org.test.service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.test.constant.ApplicationConstant;
import org.test.exception.AccountException;
import org.test.model.dto.AccountDto;
import org.test.model.entity.Account;
import org.test.model.entity.Bank;
import org.test.model.entity.Customer;
import org.test.model.enumType.SavingOrCurrentBalance;
import org.test.repo.AccountRepo;
import org.test.repo.BankRepo;
import org.test.repo.CustomerRepo;
import org.test.service.AccountService;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class AccountServiceImpl implements AccountService {

    @Inject
    BankRepo bankRepo;

    @Inject
    AccountRepo accountRepo;

    @Inject
    CustomerRepo customerRepo;

    public static String generateRandomString(int length, boolean letters, boolean numbers) {
        String characters = "";
        if (letters) {
            characters += "abcdefghijklmnopqrstuvwxyz";
        }
        if (numbers) {
            characters += "0123456789";
        }
        if (characters.isEmpty()) {
            throw new IllegalArgumentException("At least one of letters or numbers must be true");
        }
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append(characters.charAt(new Random().nextInt(characters.length())));
        }
        return result.toString();
    }

    private static void accountTypeBalance(List<Account> byCustomerAndBankAndAccountType, Account account) {
        if (!byCustomerAndBankAndAccountType.isEmpty()) {
            throw new AccountException(String.format("Customer already have %s account", account.getAccountType()), Response.Status.BAD_GATEWAY);
        } else if ((account.getAccountType().name().equals(SavingOrCurrentBalance.SAVING.name())) && (account.getAmount() < SavingOrCurrentBalance.SAVING.getAmount())) {
            throw new AccountException(ApplicationConstant.MINIMUM_BALANCE_FOR + " saving account 5000", Response.Status.BAD_REQUEST);
        } else if ((account.getAccountType().name().equals(SavingOrCurrentBalance.CURRENT.name())) && (account.getAmount() < SavingOrCurrentBalance.CURRENT.getAmount())) {
            throw new AccountException(ApplicationConstant.MINIMUM_BALANCE_FOR + " current account 10000", Response.Status.BAD_REQUEST);
        }
    }

    @Transactional
    @Override
    public String saveAccountNo(AccountDto accountDto) {
        Account account = dtoToEntity(accountDto);
        List<Account> byCustomerAndBankAndAccountType = accountRepo.findByCustomerAndBankAndAccountType(account.getCustomer(), account.getBank(), account.getAccountType());
        if (Objects.nonNull(byCustomerAndBankAndAccountType)) {
            accountTypeBalance(byCustomerAndBankAndAccountType, account);
        }
        generateAccountNo(account);
        accountRepo.persist(account);
        return ApplicationConstant.ACCOUNT_CREATE;
    }

    private void generateAccountNo(Account account) {
        String accountNumber;
        Account byAccNo;
        do {
            accountNumber = generateRandomString(12, false, true);
            byAccNo = accountRepo.findByAccNo(accountNumber).orElse(null);
        } while (Objects.nonNull(byAccNo));
        account.setAccNo(accountNumber);
    }

    private Account dtoToEntity(AccountDto accountDto) {
        Account account = new Account();
        account.setAccountType(accountDto.getAccountType());
        Bank byId = bankRepo.findById(accountDto.getBankId());
        if (Objects.isNull(byId)) {
            throw new AccountException("The bank " + ApplicationConstant.ID_INVALID + accountDto.getBankId(), Response.Status.BAD_REQUEST);
        }
        account.setBank(byId);
        Customer customer = customerRepo.findById(accountDto.getCustomerId());
        if (Objects.isNull(customer)) {
            throw new AccountException("The customer " + ApplicationConstant.ID_INVALID + accountDto.getCustomerId(), Response.Status.BAD_REQUEST);
        }
        account.setCustomer(customer);
        return account;
    }

    @Override
    public List<AccountDto> accountByAccNo(String content) {
        List<Account> accounts = accountRepo.findByTitleContent("%" + content + "%");
        if (accounts.isEmpty()) {
            throw new AccountException(ApplicationConstant.ACCOUNT_NOT_FOUND, Response.Status.NOT_FOUND);
        } else {
            return accounts.stream().map(this::entityToDto).collect(Collectors.toList());
        }
    }

    @Override
    public List<AccountDto> getAllAccount() {
        List<Account> accounts = accountRepo.findAll().list();
        if (accounts.isEmpty()) {
            throw new AccountException(ApplicationConstant.ACCOUNT_NOT_FOUND, Response.Status.NOT_FOUND);
        } else {
            return accounts.stream().map(this::entityToDto).collect(Collectors.toList());
        }
    }

    @Override
    public String updateAccount(AccountDto accountDto, Long accountId) {
        Account account = accountRepo.findById(accountId);
        if (account == null) {
            throw new AccountException(ApplicationConstant.ACCOUNT_ID_NOT_FOUND, Response.Status.NOT_FOUND);
        }
        account.setAccountType(accountDto.getAccountType());
        account.setName(accountDto.getName());
        return ApplicationConstant.ACCOUNT_UPDATED;
    }

    @Override
    public String deleteAccount(Long accountId) {
        Account account = accountRepo.findById(accountId);
        if (account == null) {
            throw new AccountException(ApplicationConstant.ACCOUNT_ID_NOT_FOUND, Response.Status.NOT_FOUND);
        }
        accountRepo.delete(account);
        return ApplicationConstant.ACCOUNT_DELETED;
    }

    @Override
    public List<AccountDto> getAllByBankId(Long bankId) {
        Bank bank = bankRepo.findById(bankId);
        if (Objects.isNull(bank)) {
            throw new AccountException(ApplicationConstant.BANK_IS_NOT_FOUND, Response.Status.BAD_GATEWAY);
        }
        List<Account> accounts = accountRepo.findByBank(bank);
        if (accounts.isEmpty()) {
            throw new AccountException(ApplicationConstant.ACCOUNT_NOT_FOUND, Response.Status.NOT_FOUND);
        }
        return accounts.stream().map(this::entityToDto).collect(Collectors.toList());
    }

    private AccountDto entityToDto(Account account) {
        AccountDto accountDto = new AccountDto();
        accountDto.setBankId(account.getBank().getBankId());
        accountDto.setCustomerId(account.getCustomer().getCustomerId());
        accountDto.setAccountId(account.getAccountId());
        accountDto.setAccountType(account.getAccountType());
        accountDto.setAmount(account.getAmount());
        accountDto.setName(account.getName());
        accountDto.setIfscCode(account.getIfscCode());
//        BeanUtils.copyProperties(account, accountDto);
        return accountDto;
    }
}
