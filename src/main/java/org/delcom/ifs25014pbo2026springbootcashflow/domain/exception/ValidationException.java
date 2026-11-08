package org.delcom.ifs25014pbo2026springbootcashflow.domain.exception;

/**
 * Dilempar ketika input dari pengguna melanggar aturan validasi domain.
 */
public class ValidationException extends DomainException {

    public ValidationException(String message) {
        super(message);
    }
}

