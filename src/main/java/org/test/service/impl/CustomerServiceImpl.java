package org.test.service.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.test.constant.ApplicationConstant;
import org.test.exception.CustomerException;
import org.test.model.dto.CustomerDto;
import org.test.model.dto.CustomerUpdateDto;
import org.test.model.entity.Account;
import org.test.model.entity.Bank;
import org.test.model.entity.Customer;
import org.test.repo.AccountRepo;
import org.test.repo.BankRepo;
import org.test.repo.CustomerRepo;
import org.test.repo.TransactionRepo;
import org.test.service.CustomerService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
//@Traced
public class CustomerServiceImpl implements CustomerService {

    @Inject
    CustomerRepo customerRepo;

    @Inject
    AccountRepo accountRepo;

    @Inject
    TransactionRepo transactionRepo;

    @Inject
    BankRepo bankRepo;

    @Transactional
    @Override
    public String save(CustomerDto customerDto) {
        String aadhaarNumber = customerDto.getAadhaarNumber();
        String panNumber = customerDto.getPanCardNumber();
        String mobileNumber = customerDto.getMobileNumber();
        String emailId = customerDto.getEmailId();

        List<Customer> byPanNuOrAadhaarNumOrMobileNum = customerRepo.findByPanCardNumberOrAadhaarNumber(panNumber, aadhaarNumber);
        List<Customer> byMobileNumberOrEmailId = customerRepo.findByMobileNumberOrEmailId(mobileNumber, emailId);
        Bank byId = bankRepo.findById(customerDto.getBankId());
        if (Objects.isNull(byId)) {
            throw new CustomerException("The bank " + ApplicationConstant.ID_INVALID + customerDto.getBankId(), Response.Status.BAD_REQUEST);
        }
        Optional<Customer> byAadhaarNumberAndPanCardNumberAndBank = customerRepo.findByAadhaarNumberAndPanCardNumberAndBank(aadhaarNumber, panNumber, byId);
        Customer customerFromDb = null;
        return customerValidation(customerDto, byAadhaarNumberAndPanCardNumberAndBank, customerFromDb, byPanNuOrAadhaarNumOrMobileNum, byMobileNumberOrEmailId, aadhaarNumber, panNumber, mobileNumber);
    }

    private String customerValidation(CustomerDto customerDto, Optional<Customer> byAadhaarNumberAndPanCardNumberAndBank, Customer customerFromDb, List<Customer> byPanNuOrAadhaarNumOrMobileNum, List<Customer> byMobileNumberOrEmailId, String aadhaarNumber, String panNumber, String mobileNumber) {
        if (byAadhaarNumberAndPanCardNumberAndBank.isPresent()) {
            customerFromDb = byAadhaarNumberAndPanCardNumberAndBank.get();
        }
        if (Objects.nonNull(customerFromDb)) {
            for (Customer customer : byPanNuOrAadhaarNumOrMobileNum) {
                if (customer.getBank().equals(customerFromDb.getBank())) {
                    throw new CustomerException(String.format("You Already registered for given bank %s", customer.getBank().getBankName()), Response.Status.BAD_REQUEST);
                }
            }
        }
        if (byPanNuOrAadhaarNumOrMobileNum != null) {
            for (Customer c : byPanNuOrAadhaarNumOrMobileNum) {
                if (c.getBank().getBankId().equals(customerDto.getBankId()) && (c.getAadhaarNumber().equals(customerDto.getAadhaarNumber()) || c.getPanCardNumber().equals(customerDto.getPanCardNumber()))) {
                    return ApplicationConstant.PAN_OR_AADHAAR_NUMBER_NOT_UNIQUE;
                }
            }
        }
        if (byMobileNumberOrEmailId != null) {
            for (Customer c : byMobileNumberOrEmailId) {
                if (c.getBank().getBankId().equals(customerDto.getBankId()) && (c.getEmailId().equals(customerDto.getEmailId()) || c.getMobileNumber().equals(customerDto.getMobileNumber()))) {
                    return ApplicationConstant.MOBILE_NUMBER_OR_EMAILID_NOT_UNIQUE;
                }
            }
        }
        if (aadhaarNumber.length() == 12 && panNumber.length() == 10 && mobileNumber.length() == 10) {
            customerRepo.persist(dtoToEntityCustomer(customerDto));
            return ApplicationConstant.CUSTOMER_CREATED;
        }
        return null;
    }

    @Override
    public List<CustomerDto> getAllCustomerByBankId(Long bankId) {
        List<Customer> customers = customerRepo.checkCustomerByBankId(bankId);
        if (customers.isEmpty()) {
            throw new CustomerException(ApplicationConstant.NO_CUSTOMER_FOR_GIVEN_BANK_ID, Response.Status.BAD_REQUEST);
        }
        return customers.stream().map(this::entityToDto).collect(Collectors.toList());
    }

    @Override
    public List<CustomerDto> getAllByBankId(Long bankId) {
        Bank bank = bankRepo.findById(bankId);
        if (Objects.isNull(bank)) {
            throw new CustomerException(ApplicationConstant.BANK_IS_NOT_FOUND, Response.Status.BAD_REQUEST);
        }
        List<Customer> customers = customerRepo.findByBank(bank);
        if (customers.isEmpty()) {
            throw new CustomerException(ApplicationConstant.CUSTOMER_NOT_FOUND_OR_CUSTOMER_DOESNT_EXIST, Response.Status.NOT_FOUND);
        }
        return customers.stream().map(this::entityToDto).collect(Collectors.toList());
    }

    private CustomerDto entityToDto(Customer customer) {
        CustomerDto customerDto = new CustomerDto();
        customerDto.setCustomerId(customer.getCustomerId());
        customerDto.setCustomerName(customer.getCustomerName());
        customerDto.setAddress(customer.getAddress());
        customerDto.setAadhaarNumber(customer.getAadhaarNumber());
        customerDto.setPanCardNumber(customer.getPanCardNumber());
        customerDto.setMobileNumber(customer.getMobileNumber());
        customerDto.setEmailId(customer.getEmailId());
        customerDto.setBankId(customer.getBank().getBankId());
        return customerDto;
    }

    private Customer dtoToEntityCustomer(CustomerDto customerDto) {
        Customer customer = new Customer();
        customer.setCustomerName(customerDto.getCustomerName());
        customer.setAddress(customerDto.getAddress());
        customer.setAadhaarNumber(customerDto.getAadhaarNumber());
        customer.setPanCardNumber(customerDto.getPanCardNumber());
        customer.setMobileNumber(customerDto.getMobileNumber());
        customer.setEmailId(customerDto.getEmailId());
        customer.setPassword(customerDto.getPassword());
        Bank bank = bankRepo.findById(customerDto.getBankId());
        if (Objects.isNull(bank)) {
            throw new CustomerException("The bank " + ApplicationConstant.ID_INVALID + customerDto.getBankId(), Response.Status.BAD_REQUEST);
        }
        customer.setBank(bank);
        return customer;
    }

    @Override
    public List<CustomerDto> getByEmailOrName(String content) {
        List<Customer> customer = customerRepo.findByTitleContent("%" + content + "%");
        if (customer.isEmpty()) {
            throw new CustomerException(ApplicationConstant.CUSTOMER_NOT_FOUND, Response.Status.NOT_FOUND);
        }
        return customer.stream().map(this::entityToDto).collect(Collectors.toList());
    }

    @Override
    public List<CustomerDto> getAllCustomer() {
        List<Customer> customers = customerRepo.findAll().list();
        if (customers.isEmpty()) {
            throw new CustomerException(ApplicationConstant.CUSTOMER_NOT_FOUND, Response.Status.BAD_REQUEST);
        }
        return customers.stream().map(this::entityToDto).collect(Collectors.toList());
    }

    @Override
    public String updateCustomer(CustomerUpdateDto customerDto, Long customerId) {
        Customer customer = customerRepo.findById(customerId);
        if (customer == null) {
            throw new CustomerException(ApplicationConstant.CUSTOMER_NOT_FOUND, Response.Status.BAD_REQUEST);
        }
        customer.setCustomerName(customerDto.getCustomerName());
        customer.setAddress(customerDto.getAddress());
        customer.setEmailId(customerDto.getEmailId());
        customer.setMobileNumber(customerDto.getMobileNumber());
        customer.setPassword(customerDto.getPassword());
        if (customerDto.getMobileNumber().length() == 10) {
            customerRepo.persist(customer);
        } else {
            throw new CustomerException(ApplicationConstant.CHECK_MOBILE_NUMBER, Response.Status.BAD_REQUEST);
        }
        return ApplicationConstant.CUSTOMER_UPDATED;
    }

    @Override
    public String deleteCustomer(Long customerId) {
        Customer customer = customerRepo.findById(customerId);
        if (customer == null) {
            throw new CustomerException(ApplicationConstant.CUSTOMER_NOT_FOUND, Response.Status.BAD_REQUEST);
        }
        List<Account> accounts = accountRepo.findByCustomer(customer);
        if (!accounts.isEmpty()) {
            throw new CustomerException(ApplicationConstant.CANT_DELETE_CUSTOMER_BECAUSE_ACCOUNT_LINKED_WITH_CUSTOMER, Response.Status.CONFLICT);
        } else {
            customerRepo.delete(customer);
        }
        return ApplicationConstant.CUSTOMER_DELETED;
    }
}
