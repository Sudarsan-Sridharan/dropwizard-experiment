package bo.gotthardt.rest;

import bo.gotthardt.AccessibleBy;
import bo.gotthardt.Persistable;
import bo.gotthardt.exception.WebAppPreconditions;
import bo.gotthardt.jersey.parameters.ListFiltering;
import bo.gotthardt.model.User;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import io.dropwizard.auth.Auth;
import lombok.RequiredArgsConstructor;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;

/**
 * @author Bo Gotthardt
 */
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class RestrictedRestResource<P extends Persistable & AccessibleBy<User>> {
    private final CrudService<P> service;

    @GET
    @Path("/{id}")
    public P one(@Auth User user, @PathParam("id") UUID id) {
        P item = service.fetchById(id);

        WebAppPreconditions.assertAccessTo(user, item);

        return item;
    }

    @GET
    public List<P> many(@Auth final User user, @Context ListFiltering filtering) {
        List<P> items = service.fetchByFilter(filtering);

        return Lists.newArrayList(Collections2.filter(items, new Predicate<P>() {
            @Override
            public boolean apply(P input) {
                return input.isAccessibleBy(user);
            }
        }));
    }

//    @POST
//    @Consumes(MediaType.APPLICATION_JSON)
//    public P create(@Auth User user, @Valid P item) {
//        // TODO
//        return service.create(item);
//    }
//
//    @PUT
//    @Path("/{id}")
//    @Consumes(MediaType.APPLICATION_JSON)
//    public P update(@Auth User user, @Valid P item, @PathParam("id") long id) {
//        WebAppPreconditions.assertAccessTo(user, item);
//
//        return service.update(id, item);
//    }

    @DELETE
    @Path("/{id}")
    public void delete(@Auth User user, @PathParam("id") UUID id) {
        P item = service.fetchById(id);

        WebAppPreconditions.assertAccessTo(user, item);

        service.delete(id);
    }
}
