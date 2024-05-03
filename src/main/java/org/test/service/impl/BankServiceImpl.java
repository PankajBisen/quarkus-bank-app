package org.test.service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.test.constant.ApplicationConstant;
import org.test.exception.BankException;
import org.test.model.dto.BankDto;
import org.test.model.entity.Account;
import org.test.model.entity.Bank;
import org.test.repo.AccountRepo;
import org.test.repo.BankRepo;
import org.test.service.BankService;

import java.util.List;
import java.util.Objects;

@ApplicationScoped
@Slf4j
public class BankServiceImpl implements BankService {

    @Inject
    BankRepo bankRepo;

    @Inject
    AccountRepo accountRepo;

    @Transactional
    @Override
    public String addBank(BankDto bankDto) {
        if (bankDto.getIfscCode().length() != 11) {
            throw new BankException(ApplicationConstant.INVALID_IFSC_CODE, Response.Status.BAD_GATEWAY);
        }
        bankRepo.findByIfscCode(bankDto.getIfscCode()).ifPresentOrElse((e) -> {
            throw new BankException(ApplicationConstant.BANK_ALREADY_REGISTER_FOR_THIS_IFSC_CODE, Response.Status.NOT_FOUND);
        }, () -> {
            bankRepo.persist(dtoToEntity(bankDto));
        });
        return ApplicationConstant.BANK_CREATED;
    }

    @Override
    public Bank getBankByName(Long content) {
        Bank byBankNameLikeOrIfscCodeLike = bankRepo.findById(content);
        if (Objects.isNull(byBankNameLikeOrIfscCodeLike)) {
            throw new BankException(ApplicationConstant.BANK_IS_NOT_FOUND, Response.Status.NOT_FOUND);
        }
        return byBankNameLikeOrIfscCodeLike;
    }

    @Override
    public String updateBank(BankDto bankDto, Long bankId) {
        bankRepo.findByIfscCode(bankDto.getIfscCode()).ifPresent((e) -> {
            throw new BankException(ApplicationConstant.BANK_ALREADY_REGISTER_FOR_THIS_IFSC_CODE, Response.Status.CONFLICT);
        });
        Bank bank = bankRepo.findById(bankId);
        if (Objects.nonNull(bank)) {
            bank = dtoToEntity(bankDto);
            bankRepo.persist(bank);
            return ApplicationConstant.BANK_UPDATE;
        } else {
            throw new BankException(ApplicationConstant.BANK_IS_NOT_FOUND, Response.Status.BAD_REQUEST);
        }
    }

    @Override
    public Response deleteBank(Long bankId) {
        Bank bank = bankRepo.findById(bankId);
        if (Objects.nonNull(bank)) {
            List<Account> accounts = accountRepo.findByBank(bank);
            if (!accounts.isEmpty()) {
                throw new BankException(ApplicationConstant.CANT_DELETE_BANK_BECAUSE_ACCOUNT_PRESENT, Response.Status.BAD_REQUEST);
            } else {
                bankRepo.delete(bank);
                return Response.ok().build();
            }
        } else {
            throw new BankException(ApplicationConstant.BANK_IS_NOT_FOUND, Response.Status.NOT_FOUND);
        }
    }

    @Override
    public List<Bank> getAllBank() {
        List<Bank> banks = bankRepo.findAll().list();
        if (banks.isEmpty()) {
            throw new BankException(ApplicationConstant.BANK_IS_NOT_FOUND, Response.Status.BAD_REQUEST);
        }
        return banks;
    }

    private Bank dtoToEntity(BankDto bankDto) {
        Bank bank = new Bank();
        bank.setBankName(bankDto.getBankName());
        bank.setAddress(bankDto.getAddress());
        bank.setCity(bankDto.getCity());
        bank.setIfscCode(bankDto.getIfscCode());
        bank.setBranchName(bankDto.getBranchName());
//        BeanUtils.copyProperties(bankDto, bank);
        return bank;
    }
}
