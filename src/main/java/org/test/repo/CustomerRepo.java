package org.test.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.test.model.entity.Bank;
import org.test.model.entity.Customer;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CustomerRepo implements PanacheRepositoryBase<Customer, Long> {

    public Optional<Customer> findByAadhaarNumberAndPanCardNumberAndBank(String aadhar, String pan, Bank bank) {
        return find("aadhaarNumber = ?1 and panCardNumber = ?2 and bank = ?3", aadhar, pan, bank).stream().findAny();
    }

    public List<Customer> findByTitleContent(String content) {
        return list("customerName like ?1 or mobileNumber like ?1 or emailId like ?1", "%" + content + "%");
    }

    public List<Customer> checkCustomerByBankId(long bankId) {
        return list("select c from Customer c left join Account a on a.customer.id = c.id where c.bank.id = ?1 and a.customer.id is null", bankId);
    }

    public List<Customer> findByPanCardNumberOrAadhaarNumber(String panNumber, String aadharNumber) {
        return list("panCardNumber = ?1 or aadhaarNumber = ?2", panNumber, aadharNumber);
    }

    public List<Customer> findByMobileNumberOrEmailId(String mobileNumber, String emailId) {
        return list("mobileNumber = ?1 or emailId = ?2", mobileNumber, emailId);
    }

    public List<Customer> findByBank(Bank bank) {
        return list("bank = ?1", bank);
    }
}
