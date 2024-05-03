package org.test.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.test.model.entity.Transaction;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class TransactionRepo implements PanacheRepositoryBase<Transaction, Long> {

    public List<Transaction> findByAccountNumberFrom(String accNo) {
        return list("accountNumberFrom", accNo);
    }

    public List<Transaction> findByDateBetween(LocalDate startDate, LocalDate endDate) {
        return list("date between ?1 and ?2", startDate, endDate);
    }
}
