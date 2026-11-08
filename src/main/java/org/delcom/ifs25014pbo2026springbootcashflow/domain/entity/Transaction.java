package org.delcom.ifs25014pbo2026springbootcashflow.domain.entity;

import java.time.LocalDateTime;

/**
 * Entitas domain yang merepresentasikan satu catatan transaksi keuangan (Cashflow).
 */
public class Transaction {

    private final int id;
    private String description;
    private double amount;
    private TransactionType type;
    private final LocalDateTime createdAt;

    public Transaction(int id, String description, double amount, TransactionType type) {
        this(id, description, amount, type, LocalDateTime.now());
    }

    public Transaction(int id, String description, double amount, TransactionType type, LocalDateTime createdAt) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void changeDescription(String newDescription) {
        this.description = newDescription;
    }

    public void changeAmount(double newAmount) {
        this.amount = newAmount;
    }

    public void changeType(TransactionType newType) {
        this.type = newType;
    }
}

