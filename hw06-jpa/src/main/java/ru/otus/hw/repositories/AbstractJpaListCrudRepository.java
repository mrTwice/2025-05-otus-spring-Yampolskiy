package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractJpaListCrudRepository<T, ID> implements ListCrudRepository<T, ID> {

    protected final EntityManager em;
    private final Class<T> entityClass;
    private final String entityName;
    private final PersistenceUnitUtil pu;

    protected AbstractJpaListCrudRepository(EntityManager em, Class<T> entityClass) {
        this.em = em;
        this.entityClass = entityClass;
        this.pu = em.getEntityManagerFactory().getPersistenceUnitUtil();
        this.entityName = em.getMetamodel().entity(entityClass).getName();
    }

    @Override
    public List<T> findAll() {
        return em.createQuery("select e from " + entityName + " e", entityClass).getResultList();
    }

    @Override
    public List<T> findAllById(Collection<ID> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return em.createQuery("select e from " + entityName + " e where e.id in :ids", entityClass)
                .setParameter("ids", ids)
                .getResultList();
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(em.find(entityClass, id));
    }

    @Override
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }

    @Override
    public long count() {
        return em.createQuery("select count(e) from " + entityName + " e", Long.class)
                .getSingleResult();
    }

    @Override
    public T save(T entity) {
        Object id = pu.getIdentifier(entity);
        if (id == null) { em.persist(entity); return entity; }
        return em.merge(entity);
    }

    @Override
    public List<T> saveAll(Collection<T> entities) {
        if (entities == null || entities.isEmpty()) return List.of();
        return entities.stream().map(this::save).collect(Collectors.toList());
    }

    @Override
    public void deleteById(ID id) {
        T ref = em.find(entityClass, id);
        if (ref != null) em.remove(ref);
    }

    @Override
    public void delete(T entity) {
        if (entity == null) return;
        if (em.contains(entity)) em.remove(entity);
        else em.remove(em.merge(entity));
    }

    @Override
    public void deleteAllById(Collection<ID> ids) {
        if (ids == null || ids.isEmpty()) return;
        ids.forEach(this::deleteById);
    }

    @Override
    public void deleteAll(Collection<T> entities) {
        if (entities == null || entities.isEmpty()) return;
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        em.createQuery("delete from " + entityName + " e").executeUpdate();
    }

    protected String getEntityName() { return entityName; }
    protected Class<T> getEntityClass() { return entityClass; }
}