package org.delcom.ifs25014pbo2026springbootcashflow.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Port generik untuk operasi penyimpanan entitas {@code T} dengan kunci {@code ID}.
 *
 * <p>Interface ini berada di lapisan {@code domain} sehingga {@code usecase} hanya
 * bergantung pada abstraksi, sementara implementasi konkretnya berada di lapisan
 * {@code adapter}.</p>
 */
public interface IRepository<T, ID> {

    List<T> findAll();

    Optional<T> findById(ID id);

    List<T> findBy(Predicate<T> predicate);

    T save(T entity);

    boolean deleteById(ID id);
}

