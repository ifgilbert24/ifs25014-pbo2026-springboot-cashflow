package org.delcom.ifs25014pbo2026springbootcashflow.domain.exception;

/**
 * Dilempar ketika entitas yang dicari berdasarkan ID tidak ditemukan di repositori.
 */
public class EntityNotFoundException extends DomainException {

    private final int id;

    public EntityNotFoundException(int id) {
        super("Transaksi dengan ID " + id + " tidak ditemukan.");
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

