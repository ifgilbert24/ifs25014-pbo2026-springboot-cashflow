package org.delcom.ifs25014pbo2026springbootcashflow.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.repository.ITransactionRepository;

/**
 * Orkestrasi logika bisnis untuk fitur Catatan Keuangan (Cashflow).
 *
 * <p>Kelas ini murni berisi aturan bisnis dan hanya bergantung pada port
 * {@link ITransactionRepository} di lapisan domain, tanpa ketergantungan pada I/O,
 * konsol, ataupun komponen UI.</p>
 */
public class FinanceUseCase {

    private final ITransactionRepository repository;

    public FinanceUseCase(ITransactionRepository repository) {
        this.repository = repository;
    }

    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }

    public Transaction addIncome(String description, double amount) throws ValidationException {
        return addTransaction(description, amount, TransactionType.INCOME);
    }

    public Transaction addExpense(String description, double amount) throws ValidationException {
        return addTransaction(description, amount, TransactionType.EXPENSE);
    }

    public Transaction addTransaction(String description, double amount, TransactionType type)
            throws ValidationException {
        validateDescription(description);
        validateAmount(amount);
        validateType(type);
        return repository.save(description.trim(), amount, type);
    }

    public Transaction updateTransaction(int id, String newDescription, Double newAmount, TransactionType newType)
            throws EntityNotFoundException, ValidationException {
        Transaction transaction = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));

        if (newDescription != null) {
            validateDescription(newDescription);
            transaction.changeDescription(newDescription.trim());
        }
        if (newAmount != null) {
            validateAmount(newAmount);
            transaction.changeAmount(newAmount);
        }
        if (newType != null) {
            transaction.changeType(newType);
        }
        return repository.save(transaction);
    }

    public void removeTransaction(int id) throws EntityNotFoundException {
        if (!repository.deleteById(id)) {
            throw new EntityNotFoundException(id);
        }
    }

    public List<Transaction> searchTransactions(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return repository.findBy(t -> t.getDescription().toLowerCase(Locale.ROOT).contains(normalized));
    }

    public List<Transaction> sortTransactions(SortOption option) {
        List<Transaction> sorted = new ArrayList<>(repository.findAll());
        sorted.sort(option.comparator());
        return sorted;
    }

    public double getTotalIncome() {
        return repository.findAll().stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpense() {
        return repository.findAll().stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    private void validateDescription(String description) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan transaksi tidak boleh kosong.");
        }
    }

    private void validateAmount(double amount) throws ValidationException {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            throw new ValidationException("Jumlah transaksi harus berupa angka lebih dari nol.");
        }
    }

    private void validateType(TransactionType type) throws ValidationException {
        if (type == null) {
            throw new ValidationException("Tipe transaksi tidak boleh kosong.");
        }
    }
}
