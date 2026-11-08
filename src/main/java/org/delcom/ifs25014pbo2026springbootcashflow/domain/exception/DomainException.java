package org.delcom.ifs25014pbo2026springbootcashflow.domain.exception;

/**
 * Base exception untuk seluruh kesalahan bisnis pada lapisan domain dan usecase.
 */
public class DomainException extends Exception {

    public DomainException(String message) {
        super(message);
    }
}

