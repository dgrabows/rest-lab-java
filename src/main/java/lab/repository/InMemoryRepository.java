package lab.repository;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * A thread-safe in memory repository.
 *
 * S and T must have well-behaved implementations of equals() and hashCode() with value semantics.
 *
 * @param <S> Type of key value uniquely identifying entities.
 * @param <T> Type of entity values stored in repository.
 */
public class InMemoryRepository<S, T> {
    private final ConcurrentMap<S, T> entities = Maps.newConcurrentMap();
    private final IdGenerator<S> idGenerator;

    private final Function<Map.Entry<S, T>, Entity<S, T>> mapToEntities = new Function<Map.Entry<S, T>, Entity<S, T>>() {
        @Nullable
        @Override
        public Entity<S, T> apply(@Nullable Map.Entry<S, T> input) {
            return input == null ? null : Entity.of(input.getKey(), input.getValue());
        }
    };

    public InMemoryRepository(IdGenerator<S> idGenerator) {
        this.idGenerator = idGenerator;
    }

    public Optional<Entity<S, T>> find(S key) {
        T value = entities.get(key);
        return Optional.fromNullable(value == null ? null : Entity.of(key, value));
    }

    public ImmutableSet<Entity<S, T>> getAll() {
        return ImmutableSet.copyOf(Iterables.transform(entities.entrySet(), mapToEntities));

    }

    /**
     * Adds an entity to the repository. A new identifier is generated and included in the return value.
     *
     * @param value value of the current state of the entity to be created
     * @return The entity with the newly generated id and current value.
     */
    public Entity<S, T> add(T value) {
        S id = idGenerator.generateId();
        T previousValue = entities.putIfAbsent(id, value);
        if (previousValue != null) {
            throw new IllegalStateException(String.format("A entity already existed with the newly generated unique id value of %s.", id));
        }
        return Entity.of(id, value);
    }

    /**
     * Replaces the current value of the entity in the repository. Only succeeds if an entity with the unique identifier
     * of the provided entity exists.
     *
     * @param entity New value of the entity.
     * @return true if entity value successfully replaced; false if no entity with identifier present
     */
    public boolean replace(Entity<S, T> entity) {
        T previousValue = entities.replace(entity.getId(), entity.getValue());
        return previousValue != null;
    }

    /**
     * Replaces the current value of the entity identified by key with newValue if an entity exists for that identifier
     * and the current value of the entity matches expected current value.
     *
     * @param key identifier for entity
     * @param expectedCurrentValue expected current value of entity; must match for change to succeed
     * @param newValue new value for entity
     * @return true if update succeeds; false if unsuccessful
     */
    public boolean replace(S key, T expectedCurrentValue, T newValue) {
        return entities.replace(key, expectedCurrentValue, newValue);
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
}
