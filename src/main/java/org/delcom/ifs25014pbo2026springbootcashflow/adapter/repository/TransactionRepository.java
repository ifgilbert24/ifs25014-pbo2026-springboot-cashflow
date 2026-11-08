package org.delcom.ifs25014pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25014pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.repository.ITransactionRepository;

/**
 * Adapter repositori in-memory untuk {@link Transaction} dengan auto-increment ID.
 */
public class TransactionRepository extends InMemoryRepository<Transaction, Integer>
        implements ITransactionRepository {

    private int idCounter = 1;

    public TransactionRepository() {
        super(Transaction::getId);
    }

    @Override
    public Transaction save(String description, double amount, TransactionType type) {
        Transaction transaction = new Transaction(idCounter++, description, amount, type);
        return super.save(transaction);
    }
}

