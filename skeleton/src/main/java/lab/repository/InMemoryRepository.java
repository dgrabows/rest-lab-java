package lab.repository;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import java.util.concurrent.ConcurrentMap;

/**
 * A thread-safe in memory repository.
 *
 * S must have well-behaved implementations of equals() and hashCode().
 *
 * @param <S> Type of key value uniquely identifying entities.
 * @param <T> Type of entities stored in repository.
 */
public class InMemoryRepository<S, T extends Identifiable<S, T>> {
    private final ConcurrentMap<S, T> entities = Maps.newConcurrentMap();
    private final IdGenerator<S> idGenerator;

    public InMemoryRepository(IdGenerator<S> idGenerator, Initializer<S, T> initializer) {
        this.idGenerator = idGenerator;
        initializer.setupData(this);
    }

    public Optional<T> find(S key) {
        return Optional.fromNullable(entities.get(key));
    }

    public ImmutableSet<T> getAll() {
        return ImmutableSet.copyOf(entities.values());
    }

    /**
     * Adds an entity to the repository. If the identifier for the entity is null, a new one will be generated and
     * included in the return value. If the identifier for the entity is not null, the provided value will be used
     * to index the entity. In this case, is the caller's responsibility to ensure that the identifier value is unique
     * and not already present in the repository.
     *
     * @param entity entity to add
     * @return A new instance of the entity with the unique identifier set, if it was not previously. The provided
     * instance if the unique identifier was provided.
     * @throws IllegalArgumentException Thrown when an entity already exists for the provided unique identifier.
     * @throws IllegalStateException Thrown when an entity already exists for a unique identifier newly generated by the
     * repository.
     */
    public T add(T entity) {
        if (entity.getId() != null) {
            T previousValue = entities.putIfAbsent(entity.getId(), entity);
            if (previousValue != null) {
                // todo create a specific exception (possibly checked) for this situation
                throw new IllegalArgumentException(String.format("A entity with id %s already exists.", entity.getId()));
            }
            return entity;
        } else {
            S id = idGenerator.generateId();
            T entityWithId = entity.withId(id);
            T previousValue = entities.putIfAbsent(id, entityWithId);
            if (previousValue != null) {
                throw new IllegalStateException(String.format("A entity already existed with the newly generated unique id value of %s.", id));
            }
            return entityWithId;
        }
    }

    /**
     * Replaces the current value of entity in the repository. Only succeeds if an entity with the unique identifier of
     * the provided entity exists.
     *
     * @param entity New value of the entity.
     * @return true if entity value successfully replaced; false if no entity with identifier present
     */
    public boolean replace(T entity) {
        T previousValue = entities.replace(entity.getId(), entity);
        return previousValue != null;
    }

    /**
     * Saves the provided entity, generating a new unique identifier, if needed. Replaces any existing value for the
     * entity.
     *
     * @param entity entity value to save
     * @return entity with identifier set; new instance if identifier was generated by the repository
     */
    public T addOrReplace(T entity) {
        if (entity.getId() == null) {
            return add(entity);
        } else {
            entities.put(entity.getId(), entity);
            return entity;
        }
    }

    /**
     * Deletes the entity with the provided key, if one exists.
     *
     * @param key unique identifier for entity to delete from repository
     * @return true if an entity was deleted; false if no entity exists matching for the provided key
     */
    public boolean delete(S key) {
        return entities.remove(key) != null;

    }

    /**
     * Generates unique identifiers of type S. Must be thread-safe.
     * @param <S> Type of identifiers generated.
     */
    public static interface IdGenerator<S> {
        S generateId();
    }

    /**
     * Populates the repository with an initial set of entities.
     *
     * @param <S> Type of identifiers.
     * @param <T> Type of entities.
     */
    public static interface Initializer<S, T extends Identifiable<S, T>> {
        void setupData(InMemoryRepository<S, T> repository);
    }
}
