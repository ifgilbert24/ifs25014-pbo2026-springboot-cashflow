package org.delcom.ifs25014pbo2026springbootcashflow.domain.entity;

import java.util.Comparator;

/**
 * Enumerasi opsi pengurutan untuk daftar {@link Transaction}.
 */
public enum SortOption {

    AMOUNT_ASC(Comparator.comparingDouble(Transaction::getAmount)),
    AMOUNT_DESC(Comparator.comparingDouble(Transaction::getAmount).reversed()),
    INCOME_FIRST(Comparator.comparing(Transaction::getType)),
    EXPENSE_FIRST(Comparator.comparing(Transaction::getType).reversed());

    private final Comparator<Transaction> comparator;

    SortOption(Comparator<Transaction> comparator) {
        this.comparator = comparator;
    }

    public Comparator<Transaction> comparator() {
        return comparator;
    }
}

