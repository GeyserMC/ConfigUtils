package org.geysermc.configutils.node.context.option;

import java.util.Objects;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.configutils.loader.validate.Validations;
import org.geysermc.configutils.parser.placeholder.Placeholders;

public class NodeOptions {
  private final CodecOptions codecOptions;
  private final Placeholders placeholders;
  private final Validations validations;
  private final Function<String, String> commentTranslator;

  private NodeOptions(
      @NonNull CodecOptions codecOptions,
      @NonNull Placeholders placeholders,
      @NonNull Validations validations,
      @NonNull Function<String, String> commentTranslator
  ) {
    this.codecOptions = Objects.requireNonNull(codecOptions);
    this.placeholders = Objects.requireNonNull(placeholders);
    this.validations = Objects.requireNonNull(validations);
    this.commentTranslator = Objects.requireNonNull(commentTranslator);
  }

  public @NonNull CodecOptions codec() {
    return codecOptions;
  }

  public @NonNull Placeholders placeholders() {
    return placeholders;
  }

  public @NonNull Validations validations() {
    return validations;
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
    private CodecOptions codecOptions;
    private Placeholders placeholders;
    private Validations validations;
    private Function<String, String> commentTranslator;

    private Builder() {
    }

    public @Nullable CodecOptions codecOptions() {
      return codecOptions;
    }

    public Builder codecOptions(@Nullable CodecOptions codecOptions) {
      this.codecOptions = codecOptions;
      return this;
    }

    public @Nullable Placeholders placeholders() {
      return placeholders;
    }

    public Builder placeholders(@Nullable Placeholders placeholders) {
      this.placeholders = placeholders;
      return this;
    }

    public Validations validations() {
      return validations;
    }

    public Builder validations(@Nullable Validations validations) {
      this.validations = validations;
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
          codecOptions != null ? codecOptions : CodecOptions.defaults(),
          placeholders != null ? placeholders : new Placeholders(),
          validations != null ? validations : Validations.builder().build(),
          commentTranslator != null ? commentTranslator : input -> input
      );
    }
  }
}
