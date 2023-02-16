package org.geysermc.configutils.node.context;

import java.util.Objects;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.configutils.util.Utils;

public final class NodeOptions {
  private final Function<String, String> nameEncoder;
  private final Function<String, String> enumDecoder;
  private final Function<String, String> enumEncoder;
  private final Function<String, String> commentTranslator;

  private NodeOptions(
      @NonNull Function<String, String> nameEncoder,
      @NonNull Function<String, String> enumDecoder,
      @NonNull Function<String, String> enumEncoder,
      @NonNull Function<String, String> commentTranslator
  ) {
    this.nameEncoder = Objects.requireNonNull(nameEncoder);
    this.enumDecoder = Objects.requireNonNull(enumDecoder);
    this.enumEncoder = Objects.requireNonNull(enumEncoder);
    this.commentTranslator = Objects.requireNonNull(commentTranslator);
  }

  public @NonNull Function<String, String> nameEncoder() {
    return nameEncoder;
  }

  public @NonNull Function<String, String> enumDecoder() {
    return enumDecoder;
  }

  public @NonNull Function<String, String> enumEncoder() {
    return enumEncoder;
  }

  public @NonNull Function<String, String> commentTranslator() {
    return commentTranslator;
  }

  public static NodeOptions defaults() {
    return builder().build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private Function<String, String> nameEncoder;
    private Function<String, String> enumDecoder;
    private Function<String, String> enumEncoder;
    private Function<String, String> commentTranslator;

    private Builder() {
    }

    public @Nullable Function<String, String> nameEncoder() {
      return nameEncoder;
    }

    public Builder nameEncoder(@Nullable Function<String, String> nameEncoder) {
      this.nameEncoder = nameEncoder;
      return this;
    }

    public @Nullable Function<String, String> enumDecoder() {
      return enumDecoder;
    }

    public Builder enumDecoder(@Nullable Function<String, String> enumDecoder) {
      this.enumDecoder = enumDecoder;
      return this;
    }

    public @Nullable Function<String, String> enumEncoder() {
      return enumEncoder;
    }

    public Builder enumEncoder(@Nullable Function<String, String> enumEncoder) {
      this.enumEncoder = enumEncoder;
      return this;
    }

    public @Nullable Function<String, String> commentTranslator() {
      return commentTranslator;
    }

    public Builder commentTranslator(@Nullable Function<String, String> commentTranslator) {
      this.commentTranslator = commentTranslator;
      return this;
    }

    public NodeOptions build() {
      return new NodeOptions(
          nameEncoder != null ? nameEncoder : Utils::camelCaseToKebabCase,
          enumDecoder != null ? enumDecoder : Utils::kebabCaseToConstantCase,
          enumEncoder != null ? enumEncoder : Utils::constantCaseToKebabCase,
          commentTranslator != null ? commentTranslator : input -> input
      );
    }
  }
}
