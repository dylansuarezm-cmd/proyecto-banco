package application.adapters.persistence.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Small helper shared by the persistence adapters. All the work runs inside the transaction opened by the
 * REST layer, so objects loaded here stay managed until the request ends and changes made by the domain are
 * written to MySQL automatically.
 */
@Component
public class JpaSupport {

    @PersistenceContext
    private EntityManager entityManager;

    public EntityManager em() {
        return entityManager;
    }

    public <T> Optional<T> findByIdentifier(String entityName, Class<T> type, String identifier) {
        return findOne("select e from " + entityName + " e where e.identifier = :id", type, Map.of("id", identifier));
    }

    public <T> Optional<T> findOne(String jpql, Class<T> type, Map<String, Object> parameters) {
        TypedQuery<T> query = entityManager.createQuery(jpql, type);
        parameters.forEach(query::setParameter);
        query.setMaxResults(1);
        List<T> result = query.getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public <T> List<T> findAll(String jpql, Class<T> type, Map<String, Object> parameters) {
        TypedQuery<T> query = entityManager.createQuery(jpql, type);
        parameters.forEach(query::setParameter);
        return query.getResultList();
    }

    public boolean exists(String jpqlCount, Map<String, Object> parameters) {
        TypedQuery<Long> query = entityManager.createQuery(jpqlCount, Long.class);
        parameters.forEach(query::setParameter);
        return query.getSingleResult() > 0;
    }

    public boolean existsByIdentifier(String entityName, String identifier) {
        return exists("select count(e) from " + entityName + " e where e.identifier = :id", Map.of("id", identifier));
    }

    /**
     * Saves a new entity (persist) or does nothing for one that is already managed, since changes to managed
     * entities are flushed at the end of the transaction.
     */
    public <T> T save(T entity) {
        if (entityManager.contains(entity)) {
            return entity;
        }
        Object id = entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
        if (id != null && entityManager.find(entity.getClass(), id) != null) {
            return entityManager.merge(entity);
        }
        entityManager.persist(entity);
        return entity;
    }
}
