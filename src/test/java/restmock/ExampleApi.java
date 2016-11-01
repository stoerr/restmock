package restmock;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/testrest")
@Consumes(MediaType.APPLICATION_OCTET_STREAM)
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class ExampleApi {

    @Path("/hello/{name}")
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@PathParam("name") String name, @QueryParam("greeting") String greeting) {
        throw new UnsupportedOperationException("Not implemented so far");
    }
}
