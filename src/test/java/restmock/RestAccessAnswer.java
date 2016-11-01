package restmock;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="http://www.stoerr.net/">Hans-Peter Stoerr</a>
 * @since 01.11.2016
 */
public class RestAccessAnswer<T> implements Answer<T> {

    private final String url;

    public RestAccessAnswer(String url) {
        this.url = url;
    }

    public T answer(InvocationOnMock invocationOnMock) throws Throwable {
        WebTarget target = ClientBuilder.newClient().target(url);
        Method method = invocationOnMock.getMethod();
        for (Path p : collectAnnotations(method, Path.class)) target = target.path(p.value());
        Annotation[][] parameterInfos = method.getParameterAnnotations();
        for (int i = 0; i < parameterInfos.length; i++) {
            Object parameter = invocationOnMock.getArgument(i);
            for (Annotation annotation : parameterInfos[i]) {
                if (annotation instanceof PathParam) {
                    target = target.resolveTemplate(((PathParam) annotation).value(), parameter);
                } else if (annotation instanceof QueryParam) {
                    target = target.queryParam(((QueryParam) annotation).value(), parameter);
                }
            }
        }
        return (T) (Object) target.getUri().toString();
    }

    private <T extends Annotation> List<T> collectAnnotations(Method method, Class<T> annotationClass) {
        List<T> result = new ArrayList<T>();
        collectAnnotationsOnClasses(method.getDeclaringClass(), annotationClass, result);
        if (null != method.getAnnotation(annotationClass)) {
            result.add(method.getAnnotation(annotationClass));
        }
        return result;
    }

    private <T extends Annotation> void collectAnnotationsOnClasses(Class<?> clazz, Class<T> annotationClass, List<T> result) {
        if (null == clazz) return;
        collectAnnotationsOnClasses(clazz.getSuperclass(), annotationClass, result);
        for (Class<?> itf : clazz.getInterfaces())
            collectAnnotationsOnClasses(itf, annotationClass, result);
        T annotation = clazz.getAnnotation(annotationClass);
        if (null != annotation) {
            result.add(annotation);
        }
    }
}
