package org.geysermc.configutils.node.context.option;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.geysermc.configutils.loader.validate.Validations;
import org.geysermc.configutils.parser.placeholder.Placeholders;

public class NodeOptions {
  private final CodecOptions codecOptions;
  private final Placeholders placeholders;
  private final Validations validations;
  private final Function<String, String> commentTranslator;
  private final boolean commentsEverywhere;

  private NodeOptions(
      @NonNull CodecOptions codecOptions,
      @NonNull Placeholders placeholders,
      @NonNull Validations validations,
      @NonNull Function<String, String> commentTranslator,
      boolean commentsEverywhere
  ) {
    this.codecOptions = Objects.requireNonNull(codecOptions);
    this.placeholders = Objects.requireNonNull(placeholders);
    this.validations = Objects.requireNonNull(validations);
    this.commentTranslator = Objects.requireNonNull(commentTranslator);
    this.commentsEverywhere = commentsEverywhere;
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

  public boolean commentsEverywhere() {
    return commentsEverywhere;
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
    private boolean commentsEverywhere;

    private Builder() {
    }

    public @Nullable CodecOptions codecOptions() {
      return codecOptions;
    }

    public @This Builder codecOptions(@Nullable CodecOptions codecOptions) {
      this.codecOptions = codecOptions;
      return this;
    }

    public @Nullable Placeholders placeholders() {
      return placeholders;
    }

    public @This Builder placeholders(@Nullable Placeholders placeholders) {
      this.placeholders = placeholders;
      return this;
    }

    public @This Builder definePlaceholder(@NonNull String name, @NonNull Supplier<Object> supplier) {
      Objects.requireNonNull(name, "Placeholder name shouldn't be null");
      Objects.requireNonNull(supplier, "Placeholder supplier shouldn't be null");
      if (placeholders == null) {
        placeholders = new Placeholders();
      }
      placeholders.addPlaceholder(name, supplier);
      return this;
    }

    public Validations validations() {
      return validations;
    }

    public @This Builder validations(@Nullable Validations validations) {
      this.validations = validations;
      return this;
    }

    public @Nullable Function<String, String> commentTranslator() {
      return commentTranslator;
    }

    public @This Builder commentTranslator(@Nullable Function<String, String> commentTranslator) {
      this.commentTranslator = commentTranslator;
      return this;
    }

    public boolean commentsEverywhere() {
      return commentsEverywhere;
    }

    public @This Builder commentsEverywhere(boolean commentsEverywhere) {
      this.commentsEverywhere = commentsEverywhere;
      return this;
    }

    public NodeOptions build() {
      return new NodeOptions(
          codecOptions != null ? codecOptions : CodecOptions.defaults(),
          placeholders != null ? placeholders : new Placeholders(),
          validations != null ? validations : Validations.builder().build(),
          commentTranslator != null ? commentTranslator : input -> input,
          commentsEverywhere
      );
    }
  }
}
