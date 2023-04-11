package org.geysermc.configutils.util;

import io.leangen.geantyref.GenericTypeReflector;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.geysermc.configutils.node.meta.Inherit;

public class ReflectionUtils {
  private static final ConcurrentMap<Method, MethodHandle> METHOD_CACHE = new ConcurrentHashMap<>();

  public static MethodHandle handleFor(Method method, Object bindTo) {
    Class<?> declaringClass = method.getDeclaringClass();
    return METHOD_CACHE.computeIfAbsent(
        method,
        $ -> {
          try {
            return MethodHandles.lookup()
                .unreflectSpecial(method, declaringClass)
                .bindTo(bindTo);
          } catch (IllegalAccessException exception) {
            throw new RuntimeException(exception);
          }
        }
    );
  }

  public static Set<Class<? extends Annotation>> inherited(AnnotatedElement type) {
    Inherit inherit = type.getAnnotation(Inherit.class);
    if (inherit == null) {
      return Collections.emptySet();
    }
    return Arrays.stream(inherit.value()).collect(Collectors.toSet());
  }

  @SuppressWarnings("unchecked")
  public static <T extends Annotation> T findAnnotation(
      Class<T> toFind,
      AnnotatedType inType
  ) {
    T annotation = inType.getAnnotation(toFind);
    if (annotation != null) {
      return annotation;
    }
    return (T) findInheritedAnnotationsMap(
        GenericTypeReflector.erase(inType.getType())
    ).get(toFind);
  }

  public static Map<Class<? extends Annotation>, Annotation> findInheritedAnnotationsMap(
      Class<?> clazz
  ) {
    return findInheritedAnnotations(clazz, (c) -> c);
  }

  public static Annotation[] findInheritedAnnotations(Method method) {
    return findInheritedAnnotationsMap(method).values().toArray(new Annotation[0]);
  }

  public static Map<Class<? extends Annotation>, Annotation> findInheritedAnnotationsMap(
      Method method
  ) {
    return findInheritedAnnotations(
        method,
        (clazz) -> {
          try {
            return clazz.getMethod(method.getName(), method.getParameterTypes());
          } catch (NoSuchMethodException ignored) {
            return null;
          }
        }
    );
  }

  public static Map<Class<? extends Annotation>, Annotation> findInheritedAnnotations(
      AnnotatedElement element,
      Function<Class<?>, AnnotatedElement> elementResolver
  ) {
    Set<Class<? extends Annotation>> toFind = inherited(element);
    Class<?> startClazz = declaringClass(element);

    Map<Class<? extends Annotation>, Annotation> result =
        findInheritedAnnotations(toFind, startClazz, elementResolver);
    if (toFind.isEmpty()) {
      return result;
    }

    throw new IllegalStateException(String.format(
        "Unable to inherit the following annotations " +
        "as they were not present on the super class / interface(s): %s",
        String.join(", ", toFind.stream().map(Class::getName).toArray(String[]::new))
    ));
  }

  private static Map<Class<? extends Annotation>, Annotation> findInheritedAnnotations(
      Set<Class<? extends Annotation>> toFind,
      Class<?> inClass,
      Function<Class<?>, AnnotatedElement> elementResolver
  ) {
    // no more classes to check
    if (inClass == null || Object.class.equals(inClass)) {
      return Collections.emptyMap();
    }

    AnnotatedElement toCheck = elementResolver.apply(inClass);
    if (toCheck == null) {
      return Collections.emptyMap();
    }

    Map<Class<? extends Annotation>, Annotation> found = new HashMap<>();

    Set<Class<? extends Annotation>> inherited = inherited(toCheck);

    Iterator<Class<? extends Annotation>> iterator = toFind.iterator();
    while (iterator.hasNext()) {
      // first check the not-inherited annotations present on the method itself
      Class<? extends Annotation> next = iterator.next();
      if (inherited.contains(next)) {
        continue;
      }

      Annotation annotation = toCheck.getAnnotation(next);
      if (annotation == null) {
        // don't fail here as there can still be another parent class/interface that
        // does have the annotation we're searching for
        continue;
      }

      found.put(next, annotation);
      iterator.remove();
    }

    if (toFind.isEmpty()) {
      return found;
    }

    Class<?> clazz = declaringClass(toCheck);

    if (clazz.getSuperclass() != null) {
      Map<Class<? extends Annotation>, Annotation> result =
          findInheritedAnnotations(toFind, clazz.getSuperclass(), elementResolver);

      for (Annotation annotation : result.values()) {
        // we don't need a contains check anywhere because it'll only return
        // annotations that we were actively searching for
        toFind.remove(annotation.annotationType());
        found.put(annotation.annotationType(), annotation);
      }

      if (toFind.isEmpty()) {
        return found;
      }
    }

    for (Class<?> parentInterface : clazz.getInterfaces()) {
      Map<Class<? extends Annotation>, Annotation> result =
          findInheritedAnnotations(toFind, parentInterface, elementResolver);

      for (Annotation annotation : result.values()) {
        toFind.remove(annotation.annotationType());
        found.put(annotation.annotationType(), annotation);
      }

      if (toFind.isEmpty()) {
        break;
      }
    }

    return found;
  }

  private static Class<?> declaringClass(AnnotatedElement element) {
    if (element instanceof Member) {
      return ((Member) element).getDeclaringClass();
    }
    // let it fail if it's not a field, method or class
    return (Class<?>) element;
  }
}
