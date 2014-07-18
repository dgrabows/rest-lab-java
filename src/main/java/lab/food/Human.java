package lab.food;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Human {
    private final String name;
    private final ImmutableSet<Favorite> favorites;

    @JsonCreator
    public Human(@JsonProperty("name") String name,
                 @JsonProperty("favorites") Iterable<Favorite> favorites) {
        this.name = checkNotNull(name);
        this.favorites = favorites == null ? ImmutableSet.<Favorite>of() : ImmutableSet.copyOf(favorites);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Human humanToCopy) {
        return new Builder(humanToCopy);
    }

    public static class Builder {
        private String name;
        private ImmutableSet<Favorite> favorites;

        public Builder() {
        }

        public Builder(Human humanToCopy) {
            this.name = humanToCopy.name;
            this.favorites = humanToCopy.favorites;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setFavorites(Iterable<Favorite> favorites) {
            this.favorites = favorites == null ? null : ImmutableSet.copyOf(favorites);
            return this;
        }

        public Builder merge(Human updates) {
            if (updates.name != null) {
                this.name = updates.name;
            }
            if (updates.favorites != null) {
                this.favorites = updates.favorites;
            }
            return this;
        }

        public Human build() {
            return new Human(name, favorites);
        }
    }

    public Human mergeIn(Human updates) {
        return builder(this).merge(updates).build();
    }

    public String getName() {
        return name;
    }

    public ImmutableSet<Favorite> getFavorites() {
        return favorites;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Human human = (Human) o;

        if (!favorites.equals(human.favorites)) return false;
        if (!name.equals(human.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + favorites.hashCode();
        return result;
    }
}
