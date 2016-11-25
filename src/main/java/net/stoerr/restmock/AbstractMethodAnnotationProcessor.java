package net.stoerr.restmock;

import static java.util.Arrays.asList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Something that processes the annotations applicable to a method: the annotations present at the
 * class, the method and the parameters themselves. Warning: this is only complete as far as used
 * in this project.
 *
 * @author <a href="http://www.stoerr.net/">Hans-Peter Stoerr</a>
 */
public abstract class AbstractMethodAnnotationProcessor {

    /**
     * Calls {@link #processClassAndMethodAnnotation(Annotation)} and {@link #processParameterAnnotation(Object, Annotation)}
     * for each annotation of the class / method / parameter.
     *
     * @param method the method
     * @param arguments the arguments of an
     */
    protected final void run(Method method, Object[] arguments) {
        for (Annotation annotation : collectAnnotations(method)) {
            processClassAndMethodAnnotation(annotation);
        }
        Annotation[][] parameterInfos = method.getParameterAnnotations();
        for (int i = 0; i < parameterInfos.length; i++) {
            for (Annotation annotation : parameterInfos[i]) {
                processParameterAnnotation(arguments[i], annotation);
            }
        }
    }

    /**
     * Called for every annotation contained in the the superclasses and the class Method is defined at.
     *
     * @param annotation some annotation
     */
    protected abstract void processClassAndMethodAnnotation(Annotation annotation);

    /**
     * Called for every parameter and every annotation on this parameter.
     *
     * @param parameter a parameter of the method
     * @param annotation some annotation at the parameter
     */
    protected abstract void processParameterAnnotation(Object parameter, Annotation annotation);

    private List<Annotation> collectAnnotations(Method method) {
        List<Annotation> result = new ArrayList<>();
        collectAnnotationsOnClasses(method.getDeclaringClass(), result);
        result.addAll(asList(method.getAnnotations()));
        return result;
    }

    private void collectAnnotationsOnClasses(Class<?> clazz, List<Annotation> result) {
        if (null == clazz) {
            return;
        }
        collectAnnotationsOnClasses(clazz.getSuperclass(), result);
        for (Class<?> itf : clazz.getInterfaces()) {
            collectAnnotationsOnClasses(itf, result);
        }
        result.addAll(asList(clazz.getAnnotations()));
    }

    /**
     * For use in the process methods: applies call to every member of collection that is of type clazz.
     *
     * @param <L> the element type of collection
     * @param <T> the (e.g. annotation) class to work on
     * @param collection a collection, not null
     * @param clazz a class that can be contained in collection
     * @param call something to do to all clazz items of collection
     */
    protected final <L, T extends L> void foreach(Collection<L> collection, Class<T> clazz, Consumer<T> call) {
        for (L l : collection) {
            if (clazz.isAssignableFrom(l.getClass())) {
                call.accept((T) l);
            }
        }
    }

}
