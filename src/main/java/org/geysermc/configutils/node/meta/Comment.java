package org.geysermc.configutils.node.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The comment to add to the given node. If no value is provided the full key of this node will be
 * used as comment. This is done so that you can easily translate them.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Comment {
  String DEFAULT = "\u0000";

  String value() default DEFAULT;
}
