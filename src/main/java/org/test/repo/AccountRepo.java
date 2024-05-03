package org.test.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.test.model.entity.Account;
import org.test.model.entity.Bank;
import org.test.model.entity.Customer;
import org.test.model.enumType.SavingOrCurrentBalance;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class AccountRepo implements PanacheRepositoryBase<Account, Long> {

    public Optional<Account> findByAccNo(String accNo) {
        return find("accNo", accNo).stream().findAny();
    }

    public List<Account> findByCustomerAndBankAndAccountType(Customer customer, Bank bank, SavingOrCurrentBalance accountType) {
        return find("customer = ?1 and bank = ?2 and accountType = ?3", customer, bank, accountType).list();
    }

    public List<Account> findByBank(Bank bank) {
        return find("bank", bank).list();
    }

    public List<Account> findByCustomer(Customer customer) {
        return find("customer", customer).list();
    }

    public List<Account> findByTitleContent(String content) {
        return find("accNo LIKE ?1", "%" + content + "%").list();
    }
}
