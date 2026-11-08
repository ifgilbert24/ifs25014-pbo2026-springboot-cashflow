package org.delcom.ifs25014pbo2026springbootcashflow.adapter.presenter;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.entity.Transaction;

/**
 * Presenter yang memformat data domain {@link Transaction} menjadi representasi teks
 * siap tampil untuk antarmuka pengguna (Web UI).
 */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public String format(Transaction transaction) {
        return String.format(
                "[%d] %s | %s | [%s]",
                transaction.getId(),
                transaction.getDescription(),
                formatCurrency(transaction.getAmount()),
                formatType(transaction));
    }

    public String formatCurrency(double amount) {
        return String.format(Locale.US, "Rp %,.2f", amount);
    }

    public String formatAmount(Transaction transaction) {
        return formatCurrency(transaction.getAmount());
    }

    public String formatType(Transaction transaction) {
        return transaction.getType().getLabel();
    }

    public String formatCreatedAt(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_FORMAT);
    }

    public String formatBalance(double balance) {
        return "Saldo: " + formatCurrency(balance);
    }

    public String showAddSuccess(Transaction transaction) {
        return "Berhasil menambahkan " + transaction.getType().getLabel().toLowerCase(Locale.ROOT)
                + ": " + transaction.getDescription();
    }

    public String showRemoveSuccess() {
        return "Berhasil menghapus transaksi!";
    }

    public String showRemoveFailed(int id) {
        return "Gagal menghapus transaksi: ID " + id + " tidak ditemukan.";
    }

    public String showUpdateSuccess() {
        return "Berhasil mengubah transaksi!";
    }

    public String showUpdateFailed(int id) {
        return "Gagal mengubah transaksi: ID " + id + " tidak ditemukan.";
    }

    public String showValidationError(String message) {
        return "Error: " + message;
    }

    public String showInvalidAmount() {
        return "Error: Jumlah transaksi harus berupa angka yang valid.";
    }

    public String showInvalidId() {
        return "Error: ID harus berupa angka.";
    }
}

