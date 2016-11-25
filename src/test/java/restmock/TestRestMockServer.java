package restmock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.stoerr.restmock.RestAccessAnswer;
import net.stoerr.restmock.RestMockServer;

/**
 * Tests RestMockServer via {@link RestAccessAnswer}.
 */
public class TestRestMockServer {

    private static RestMockServer server;

    private ExampleApi mock = server.giveMock(ExampleApi.class);

    @BeforeClass
    public static void startServer() {
        server = new RestMockServer();
    }

    @AfterClass
    public static void stopServer() {
        server.close();
    }

    /**
     * Checks what happens when we start two servers on the same port.
     */
    @Test
    public void testPortConflict() {
        Assert.assertEquals("http://localhost:50000/", server.getServerUrl());
        try (RestMockServer server2 = new RestMockServer()) {
            Assert.assertEquals("http://localhost:50001/", server2.getServerUrl());
        }
    }

    /**
     * Tests access to a REST mock initialized with mockito via an accessor implemented by {@link RestAccessAnswer}.
     */
    @Test
    public void testAccessMock() {
        when(mock.hello("Franz", "Yo")).thenReturn("Yo Franz!");

        ExampleApi accessor = RestAccessAnswer.restAccessor(ExampleApi.class, server.getServerUrl());
        assertEquals("Yo Franz!", accessor.hello("Franz", "Yo"));

        verify(mock).hello("Franz", "Yo");
    }

}
