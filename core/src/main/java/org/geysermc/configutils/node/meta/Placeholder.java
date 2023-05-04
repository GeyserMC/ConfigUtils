package org.geysermc.configutils.node.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Placeholder {
  String DEFAULT = "\u0000";

  /**
   * The identifier of the placeholder. The default identifier is the full key of the node.
   *
   * <pre>{@code
   * section:
   *  subsection:
   *    key:
   * }</pre>
   * The default identifier would be {@code section.subsection.key}
   */
  String value() default DEFAULT;
}
