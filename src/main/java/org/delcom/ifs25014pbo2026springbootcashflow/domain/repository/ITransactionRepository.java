package org.delcom.ifs25014pbo2026springbootcashflow.domain.repository;

import org.delcom.ifs25014pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.entity.TransactionType;

/**
 * Port repositori khusus untuk entitas {@link Transaction}.
 */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {

    Transaction save(String description, double amount, TransactionType type);
}

