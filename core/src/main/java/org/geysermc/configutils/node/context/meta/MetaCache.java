package org.geysermc.configutils.node.context.meta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MetaCache {
  private final Map<Class<? extends Annotation>, Object> cachedAnnotations = new HashMap<>();

  @SuppressWarnings("unchecked")
  public <A extends Annotation, T> T cacheIfAbsent(MetaCacheMapper<A, T> mapper) {
    return (T) cachedAnnotations.computeIfAbsent(mapper.annotationClass(), (aClass) -> {
      InputStream stream =
          getClass().getClassLoader().getResourceAsStream(aClass.getCanonicalName());

      List<String> lines = new ArrayList<>();
      if (stream == null) {
        lines = Collections.emptyList();
      } else {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
          while (reader.ready()) {
            lines.add(reader.readLine());
          }
        } catch (IOException exception) {
          exception.printStackTrace();
        }
      }

      return mapper.map(lines);
    });
  }
}
