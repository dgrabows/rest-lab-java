package lab.repository;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import static com.google.common.base.Preconditions.checkNotNull;

public class Entity<S, T> {
    private final S id;
    private final T value;

    @JsonCreator
    public Entity(@JsonProperty("id") S id,
                  @JsonProperty("value") T value) {
        this.id = checkNotNull(id);
        this.value = checkNotNull(value);
    }

    public static <S, T> Entity<S, T> of(S id, T value) {
        return new Entity<S, T>(id, value);
    }

    public S getId() {
        return id;
    }

    @JsonUnwrapped
    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entity entity = (Entity) o;

        if (!id.equals(entity.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
