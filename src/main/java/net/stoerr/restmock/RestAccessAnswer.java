package net.stoerr.restmock;

import java.lang.annotation.Annotation;
import java.util.function.BiFunction;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mockito answer that actually accesses a REST service with a given mock.
 * This is done with mockito since we can easily not only use interfaces
 * but also classes as template for the REST calls.
 *
 * @author <a href="http://www.stoerr.net/">Hans-Peter Stoerr</a>
 * @param <T> the return type for the answer
 */
public class RestAccessAnswer<T> implements Answer<T> {

    private final String url;

    /**
     * Returns an instance derived from T that forwards all calls via REST to
     * the service according to T's annotations deployed at baseUrl (excluding the
     * paths specified at T itself).
     *
     * @param <T> the interface / base class of the service
     * @param clazz the interface / base class of the service
     * @param baseUrl the basic URL at which the service is deployed
     * @return forwarder for calls via REST to service deployed at baseUrl
     */
    public static <T> T restAccessor(Class<T> clazz, String baseUrl) {
        return Mockito.mock(clazz, new RestAccessAnswer<T>(baseUrl));
    }

    /**
     * Constructs an answer for the given URL to access the REST-Service.
     *
     * @param url the url
     */
    public RestAccessAnswer(String url) {
        this.url = url;
    }

    /** Accesses the REST-Service for the implementation. */
    @SuppressWarnings("unchecked")
    @Override
    public T answer(InvocationOnMock invocationOnMock) throws Throwable {
        final WebTarget[] targetHolder = { ClientBuilder.newClient().target(url) };
        final HttpMethod[] methodHolder = new HttpMethod[1];

        new AbstractMethodAnnotationProcessor() {

            private <A extends Annotation> void processAnnotation(Class<A> clazz, Annotation annotation,
                    BiFunction<WebTarget, A, WebTarget> call) {
                if (clazz.isAssignableFrom(annotation.getClass())) {
                    A aAnnotation = (A) annotation;
                    targetHolder[0] = call.apply(targetHolder[0], aAnnotation);
                }
            }

            @Override
            protected void processClassAndMethodAnnotation(Annotation annotation) {
                processAnnotation(Path.class, annotation, (target, path) -> target.path(path.value()));
                HttpMethod method = annotation.annotationType().getAnnotation(HttpMethod.class);
                if (null != method) {
                    methodHolder[0] = method;
                }
            }

            @Override
            protected void processParameterAnnotation(Object argument, Annotation annotation) {
                processAnnotation(PathParam.class, annotation, (target, path) -> target.resolveTemplate(path.value(), argument));
                processAnnotation(QueryParam.class, annotation, (target, path) -> target.queryParam(path.value(), argument));
            }
        }.run(invocationOnMock.getMethod(), invocationOnMock.getArguments());

        // TODO: mediatypes, request body
        Response response = targetHolder[0].request().method(methodHolder[0].value());
        if (!Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
            throw new IllegalStateException("Got wrong HTTP status " + response.getStatusInfo());
        }
        Class<?> returnType = invocationOnMock.getMethod().getReturnType();
        if (returnType.isAssignableFrom(Response.class)) {
            return (T) response;
        }
        return (T) response.readEntity(returnType);
    }

}
