package net.stoerr.restmock;

import java.net.BindException;
import java.util.HashMap;
import java.util.Map;

import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.mockito.Mockito;

/**
 * Creates a TJWSEmbeddedJaxrsServer for deploying REST Mocks created with mockito.
 * For these mocks you can define behaviour and verify expectations for REST calls with mockito.
 */
public class RestMockServer implements AutoCloseable {

    private final TJWSEmbeddedJaxrsServer tjws;

    private Map<Class<?>, Object> mocks = new HashMap<>();

    /**
     * Starts the server. Don't forget to {@link #close()}!
     */
    public RestMockServer() {
        tjws = new TJWSEmbeddedJaxrsServer();
        for (int port = 50000; port < 51000; ++port) {
            try {
                tjws.setPort(port);
                tjws.start();
                return;
            } catch (RuntimeException e) {
                if (!(e.getCause() instanceof BindException)) {
                    throw e;
                    // check with next port
                }
            }
        }
        tjws.stop();
        throw new IllegalStateException("Couldn't open port");
    }

    /**
     * The base URL of the server.
     *
     * @return the URL
     */
    public String getServerUrl() {
        return "http://localhost:" + tjws.getPort() + "/";
    }

    /**
     * Gives or creates the Mockito mock object for the clazz.
     *
     * @param <T> the type of the mock
     * @param clazz the type of the mock
     * @return the Mockito mock
     */
    @SuppressWarnings("unchecked")
    public <T> T giveMock(Class<T> clazz) {
        return (T) mocks.computeIfAbsent(clazz, (key) -> {
            T mock = Mockito.mock(clazz);
            mocks.put(clazz, mock);
            tjws.getDeployment().getRegistry().addSingletonResource(mock);
            return mock;
        });
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        tjws.stop();
    }

    /** {@inheritDoc} */
    @Override
    protected void finalize() throws Throwable {
        close();
    }

}
