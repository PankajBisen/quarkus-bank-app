package org.test.service.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.test.constant.ApplicationConstant;
import org.test.exception.TransactionException;
import org.test.model.dto.MoneyTransferDto;
import org.test.model.entity.Account;
import org.test.model.entity.Transaction;
import org.test.model.enumType.SavingOrCurrentBalance;
import org.test.repo.AccountRepo;
import org.test.repo.TransactionRepo;
import org.test.service.TransactionService;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Singleton
public class TransactionServiceImpl implements TransactionService {

    @Inject
    AccountRepo accountRepo;

    @Inject
    TransactionRepo transactionRepo;

    @Override
    @Transactional
    public String transferMoney(MoneyTransferDto transferDto) {
        Transaction transaction = new Transaction();
        Account fromAccount = accountRepo.findByAccNo(transferDto.getAccountNumberFrom()).orElseThrow(() -> new TransactionException("From " + ApplicationConstant.ACCOUNT_NOT_FOUND, Response.Status.NOT_FOUND));
        Account toAccount = accountRepo.findByAccNo(transferDto.getAccountNumberTo()).orElseThrow(() -> new TransactionException("To " + ApplicationConstant.ACCOUNT_NOT_FOUND, Response.Status.NOT_FOUND));

        moneyTransferValidation(transferDto, fromAccount, toAccount);
        double fromAccountDebited = fromAccount.getAmount() - transferDto.getAmount();
        double toAccountCredited = toAccount.getAmount() + transferDto.getAmount();

        fromAccount.setAmount(fromAccountDebited);
        toAccount.setAmount(toAccountCredited);

        transaction.setAccountNumberFrom(transferDto.getAccountNumberFrom());
        transaction.setAccountNumberTo(transferDto.getAccountNumberTo());
        transaction.setAmount(transferDto.getAmount());
        transaction.setDate(LocalDate.now());

        accountRepo.persist(fromAccount);
        accountRepo.persist(toAccount);
        transactionRepo.persist(transaction);

        saveAccountTypeTransaction(transferDto, fromAccountDebited, transaction);
        return ApplicationConstant.TRANSACTION_SUCCESSFUL;
    }

    private void moneyTransferValidation(MoneyTransferDto transferDto, Account fromAccount, Account toAccount) {
        if (fromAccount.isBlocked()) {
            throw new TransactionException("Account no is blocked: " + fromAccount.getAccNo(), Response.Status.CONFLICT);
        }
        if (!transferDto.getIfscCode().equals(toAccount.getIfscCode())) {
            throw new TransactionException(ApplicationConstant.INVALID_IFSC_CODE + ApplicationConstant.DOES_NOT_MATCH + transferDto.getIfscCode(), Response.Status.BAD_REQUEST);
        }
        if (!transferDto.getAccountType().equals(toAccount.getAccountType())) {
            throw new TransactionException(ApplicationConstant.INVALID_ACCOUNT_TYPE + ApplicationConstant.DOES_NOT_MATCH + transferDto.getAccountType(), Response.Status.BAD_REQUEST);
        }
        if (transferDto.getAmount() > fromAccount.getAmount()) {
            throw new TransactionException(ApplicationConstant.ACCOUNT_BALANCE_LOW, Response.Status.NOT_ACCEPTABLE);
        }
    }

    private void saveAccountTypeTransaction(MoneyTransferDto transferDto, double fromAccountDebited, Transaction transaction) {
        Optional<Account> fromAccount1 = accountRepo.findByAccNo(transferDto.getAccountNumberFrom());
        if (Objects.nonNull(fromAccount1)) {
            saveTransaction(fromAccount1.get(), SavingOrCurrentBalance.SAVING, fromAccountDebited < (SavingOrCurrentBalance.SAVING.getAmount()), true, transaction, ApplicationConstant.TRANSACTION_SUCCESSFUL_BUT_ACCOUNT_BLOCKED, SavingOrCurrentBalance.CURRENT, fromAccountDebited < (SavingOrCurrentBalance.CURRENT.getAmount()), ApplicationConstant.TRANSACTION_SUCCESSFUL_BUT_ACCOUNT_BLOCKED);
        }

        Optional<Account> toAccount1 = accountRepo.findByAccNo(transferDto.getAccountNumberTo());
        if (Objects.nonNull(toAccount1)) {
            saveTransaction(toAccount1.get(), SavingOrCurrentBalance.CURRENT, fromAccountDebited > (SavingOrCurrentBalance.CURRENT.getAmount()), false, transaction, ApplicationConstant.TRANSACTION_SUCCESSFUL, SavingOrCurrentBalance.SAVING, fromAccountDebited > (SavingOrCurrentBalance.SAVING.getAmount()), ApplicationConstant.TRANSACTION_SUCCESSFUL);
        }
    }

    private void saveTransaction(Account fromAccount1, SavingOrCurrentBalance saving, boolean fromAccountDebited, boolean isBlocked, Transaction transaction, String savingAcc, SavingOrCurrentBalance current, boolean fromAccountDebited1, String currentAcc) {
        if ((fromAccount1.getAccountType().name().equals(saving.name())) && fromAccountDebited) {
            fromAccount1.setBlocked(isBlocked);
            transactionRepo.persist(transaction);
        }
        if ((fromAccount1.getAccountType().name().equals(current.name())) && fromAccountDebited1) {
            fromAccount1.setBlocked(isBlocked);
            transactionRepo.persist(transaction);
        }
    }

    @Override
    public List<Transaction> transaction(String accountNumberFrom) {
        Account account = accountRepo.findByAccNo(accountNumberFrom).orElseThrow(() -> new TransactionException(ApplicationConstant.ACCOUNT_NOT_FOUND, Response.Status.NOT_FOUND));
        List<Transaction> transactions = transactionRepo.findByAccountNumberFrom(accountNumberFrom);
        if (transactions.isEmpty()) {
            throw new TransactionException(ApplicationConstant.NO_TRANSACTION, Response.Status.NOT_FOUND);
        } else return transactions;
    }

    @Override
    public List<Transaction> transactionByDays(Long numberOfDays) {
        List<Transaction> transactions = transactionRepo.findByDateBetween(LocalDate.now().minusDays(numberOfDays), LocalDate.now());
        if (transactions.isEmpty()) {
            throw new TransactionException(ApplicationConstant.NO_TRANSACTION_IN_BETWEEN_DAYS, Response.Status.NOT_FOUND);
        } else return transactions;
    }
}
