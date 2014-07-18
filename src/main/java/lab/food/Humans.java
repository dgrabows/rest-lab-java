package lab.food;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import lab.repository.Entity;
import lab.repository.InMemoryRepository;
import lab.repository.LongIdGenerator;
import lab.support.PATCH;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static javax.ws.rs.core.Response.Status;

@Controller
@Path("/humans")
@Produces(MediaType.APPLICATION_JSON)
public class Humans {
    private final InMemoryRepository<Long, Human> humanRepository = new InMemoryRepository<Long, Human>(new LongIdGenerator());

    @GET
    public Response retrieveAll() {
        return Response.ok(humanRepository.getAll()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@Context UriInfo uriInfo, Human humanValue) {
        Entity<Long, Human> human = humanRepository.add(humanValue);
        URI location = uriInfo.getAbsolutePathBuilder().path("{id}").build(human.getId());
        return Response.created(location).entity(human).build();
    }

    @GET
    @Path("/{id}")
    public Response retrieve(@PathParam("id") long id) {
        Optional<Entity<Long, Human>> human = humanRepository.find(id);
        if (human.isPresent()) {
            return Response.ok(human.get()).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response replace(@PathParam("id") long id, Human humanValue) {
        Entity<Long, Human> human = Entity.of(id, humanValue);
        if (humanRepository.replace(human)) {
            return Response.ok(human).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @PATCH
    @Path("/{id}")
    public Response update(@PathParam("id") long id, Human updates) {
        Optional<Entity<Long, Human>> currentState = humanRepository.find(id);
        if (!currentState.isPresent()) {
            return Response.status(Status.NOT_FOUND).build();
        }
        Entity<Long, Human> updatedState = Entity.of(id, currentState.get().getValue().mergeIn(updates));
        if (humanRepository.replace(updatedState)) {
            return Response.ok(updatedState).build();
        } else {
            // the human was deleted in between the retrieval and update
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") long id) {
        humanRepository.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{humanId}/favorites")
    public Response getFavorites(@PathParam("humanId") long humanId) {
        Optional<Entity<Long, Human>> human = humanRepository.find(humanId);
        if (human.isPresent()) {
            return Response.ok(human.get().getValue().getFavorites()).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/{humanId}/favorites/{mealId}")
    public Response getFavorite(@PathParam("humanId") long humanId, @PathParam("mealId") long mealId) {
        Optional<Entity<Long, Human>> human = humanRepository.find(humanId);
        Favorite favorite = Favorite.of(mealId);
        if (human.isPresent() && human.get().getValue().getFavorites().contains(favorite)) {
            return Response.ok(favorite).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    /**
     * Note: Treats addition of favorite that already exists as successful.
     */
    @PUT
    @Path("/{humanId}/favorites/{mealId}")
    public Response addFavorite(@PathParam("humanId") long humanId, @PathParam("mealId") long mealId) {
        Optional<Entity<Long, Human>> human = humanRepository.find(humanId);
        if (human.isPresent()) {
            Human currentValue = human.get().getValue();
            Favorite newFavorite = Favorite.of(mealId);
            Iterable<Favorite> newFavorites = Iterables.concat(currentValue.getFavorites(), ImmutableSet.of(newFavorite));
            Human updatedValue = Human.builder(currentValue).setFavorites(newFavorites).build();
            Entity<Long, Human> updatedState = Entity.of(humanId, updatedValue);
            if (humanRepository.replace(updatedState)) {
                return Response.ok(newFavorite).build();
            } else {
                // the human was deleted in between the retrieval and update
                return Response.status(Status.NOT_FOUND).build();
            }
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    /**
     * Note: Treats deletion of a favorite that no longer exists as successful.
     */
    @DELETE
    @Path("/{humanId}/favorites/{mealId}")
    public Response deleteFavorite(@PathParam("humanId") long humanId, @PathParam("mealId") long mealId) {
        Optional<Entity<Long, Human>> human = humanRepository.find(humanId);
        if (human.isPresent()) {
            Human currentValue = human.get().getValue();
            Favorite favoriteToDelete = Favorite.of(mealId);
            Iterable<Favorite> newFavorites = Sets.difference(currentValue.getFavorites(), ImmutableSet.of(favoriteToDelete));
            Human updatedValue = Human.builder(currentValue).setFavorites(newFavorites).build();
            Entity<Long, Human> updatedState = Entity.of(humanId, updatedValue);
            if (humanRepository.replace(updatedState)) {
                return Response.noContent().build();
            } else {
                // the human was deleted in between the retrieval and update
                return Response.status(Status.NOT_FOUND).build();
            }
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }
}
