package restmock;

import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * @author <a href="http://www.stoerr.net/">Hans-Peter Stoerr</a>
 * @since 01.11.2016
 */
public class TestRestMockExampleApi {

    private static ExampleApi mock = mock(ExampleApi.class);
    private static TJWSEmbeddedJaxrsServer tjws;

    @BeforeClass
    public static void startServer() {
        tjws = new TJWSEmbeddedJaxrsServer();
        tjws.setPort(8080);
        tjws.start();
        tjws.getDeployment().getRegistry().addSingletonResource(mock);
    }

    @AfterClass
    public static void stopServer() {
        tjws.stop();
    }

    @Test
    public void testAccessMock() {
        when(mock.hello("Franz", "Yo")).thenReturn("Yo Franz!");

        Invocation.Builder request = ClientBuilder.newClient().target("http://localhost:8080/").path("/testrest")
                .path("/hello/{name}").resolveTemplate("name", "Franz").queryParam("greeting", "Yo").request();
        Response response = request.get();
        String result = response.readEntity(String.class);
        assertEquals("Yo Franz!", result);
    }

}
