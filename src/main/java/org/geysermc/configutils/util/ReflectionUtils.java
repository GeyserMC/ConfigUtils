package org.geysermc.configutils.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
}
