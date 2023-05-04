package org.geysermc.configutils.node.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedType;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Range {
  private final NumericRange numericRange;
  private final DecimalRange decimalRange;
  private final StringRange stringRange;

  private Range(
      @Nullable NumericRange numericRange,
      @Nullable DecimalRange decimalRange,
      @Nullable StringRange stringRange
  ) {
    this.numericRange = numericRange;
    this.decimalRange = decimalRange;
    this.stringRange = stringRange;
  }

  public static Range of(AnnotatedType type) {
    return new Range(
        type.getAnnotation(NumericRange.class),
        type.getAnnotation(DecimalRange.class),
        type.getAnnotation(StringRange.class)
    );
  }

  public boolean isInRange(Object object) {
    if (object instanceof String) {
      if (stringRange != null) {
        int length = ((String) object).length();
        return length >= stringRange.from() && length <= stringRange.to();
      }
    } else if (object instanceof Number) {
      Number number = (Number) object;
      if (numericRange != null) {
        long value = number.longValue();
        return value >= numericRange.from() && value <= numericRange.to();
      }
      if (decimalRange != null) {
        double value = number.doubleValue();
        return value >= decimalRange.from() && value <= decimalRange.to();
      }
    }
    return true;
  }

  public Number from() {
    if (stringRange != null) {
      return stringRange.from();
    }
    if (numericRange != null) {
      return numericRange.from();
    }
    if (decimalRange != null) {
      return decimalRange.from();
    }
    return null;
  }

  public Number to() {
    if (stringRange != null) {
      return stringRange.to();
    }
    if (numericRange != null) {
      return numericRange.to();
    }
    if (decimalRange != null) {
      return decimalRange.to();
    }
    return null;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface NumericRange {
    /**
     * The minimal value allowed (inclusive)
     */
    long from();

    /**
     * The maximal value allowed (inclusive)
     */
    long to();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface DecimalRange {
    /**
     * The minimal value allowed (inclusive)
     */
    double from();

    /**
     * The maximal value allowed (inclusive)
     */
    double to();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface StringRange {
    /**
     * The minimal String length allowed (inclusive)
     */
    int from();

    /**
     * The maximal String length allowed (inclusive)
     */
    int to();
  }
}
