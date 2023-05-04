package org.geysermc.configutils.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public final class TypeUtils {
  public static boolean isArray(Type input) {
    if (input instanceof Class<?>) {
      return ((Class<?>) input).isArray();
    }
    if (input instanceof ParameterizedType) {
      return isArray(((ParameterizedType) input).getRawType());
    }
    return input instanceof GenericArrayType;
  }
}
