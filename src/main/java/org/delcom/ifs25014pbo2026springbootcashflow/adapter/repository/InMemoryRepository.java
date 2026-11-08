package org.delcom.ifs25014pbo2026springbootcashflow.adapter.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.repository.IRepository;

/**
 * Implementasi generik {@link IRepository} berbasis {@link ArrayList} di memori.
 */
public class InMemoryRepository<T, ID> implements IRepository<T, ID> {

    protected final List<T> storage = new ArrayList<>();
    private final Function<T, ID> idExtractor;

    public InMemoryRepository(Function<T, ID> idExtractor) {
        this.idExtractor = idExtractor;
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(storage);
    }

    @Override
    public Optional<T> findById(ID id) {
        return storage.stream()
                .filter(item -> idExtractor.apply(item).equals(id))
                .findFirst();
    }

    @Override
    public List<T> findBy(Predicate<T> predicate) {
        return storage.stream()
                .filter(predicate)
                .toList();
    }

    @Override
    public T save(T entity) {
        ID id = idExtractor.apply(entity);
        for (int i = 0; i < storage.size(); i++) {
            if (idExtractor.apply(storage.get(i)).equals(id)) {
                storage.set(i, entity);
                return entity;
            }
        }
        storage.add(entity);
        return entity;
    }

    @Override
    public boolean deleteById(ID id) {
        return storage.removeIf(item -> idExtractor.apply(item).equals(id));
    }
}

