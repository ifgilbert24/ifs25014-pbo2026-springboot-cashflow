package org.delcom.ifs25014pbo2026springbootcashflow.domain.entity;

/**
 * Enumerasi tipe transaksi keuangan: Pemasukan ({@link #INCOME}) atau Pengeluaran ({@link #EXPENSE}).
 */
public enum TransactionType {

    INCOME("Pemasukan"),
    EXPENSE("Pengeluaran");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

