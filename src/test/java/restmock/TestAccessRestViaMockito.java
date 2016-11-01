package restmock;

import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="http://www.stoerr.net/">Hans-Peter Stoerr</a>
 * @since 01.11.2016
 */
public class TestAccessRestViaMockito {

    private static ExampleApi mock = mock(ExampleApi.class);
    private static TJWSEmbeddedJaxrsServer tjws;

    @BeforeClass
    public static void startServer() {
        tjws = new TJWSEmbeddedJaxrsServer();
        tjws.setPort(8080);
        tjws.start();
        tjws.getDeployment().getRegistry().addSingletonResource(mock);

        when(mock.hello("Franz", "Yo")).thenReturn("Yo Franz!");
    }

    @AfterClass
    public static void stopServer() {
        tjws.stop();
    }

    @Test
    public void testAccessMock() {
        ExampleApi accessor = mock(ExampleApi.class, new RestAccessAnswer<ExampleApi>("http://localhost:8080/"));
        assertEquals("Yo Franz!", accessor.hello("Franz", "Yo"));
    }

}
