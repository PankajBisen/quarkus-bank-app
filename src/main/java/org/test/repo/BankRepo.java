package org.test.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.test.model.entity.Bank;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class BankRepo implements PanacheRepositoryBase<Bank, Long> {

    public Optional<Bank> findByIfscCode(String ifscCode) {
        return find("ifscCode", ifscCode).stream().findAny();
    }

//    public List<Bank> findByBankNameLikeOrIfscCodeLike(String key) {
//        return find("key", key).list();
//    }

}
