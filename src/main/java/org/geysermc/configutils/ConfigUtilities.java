package org.geysermc.configutils;

import io.leangen.geantyref.GenericTypeReflector;
import java.io.Reader;
import java.lang.reflect.AnnotatedType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.configutils.file.codec.FileCodec;
import org.geysermc.configutils.loader.ConfigLoader;
import org.geysermc.configutils.loader.validate.Validations;
import org.geysermc.configutils.node.codec.RegisteredCodecs;
import org.geysermc.configutils.node.context.NodeContext;
import org.geysermc.configutils.node.context.RootNodeContext;
import org.geysermc.configutils.node.context.option.NodeOptions;
import org.geysermc.configutils.parser.placeholder.Placeholders;
import org.geysermc.configutils.updater.ConfigUpdater;
import org.geysermc.configutils.updater.change.Changes;
import org.geysermc.configutils.updater.file.ConfigFileUpdaterResult;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

public class ConfigUtilities {
  private final FileCodec fileCodec;
  private final String configFile;
  private final String configVersionName;
  private final Changes changes;
  private final Set<String> copyDirectly;
  private final Object postInitializeCallbackArgument;
  private final boolean saveConfigAutomatically;

  private final RegisteredCodecs codecs;
  private final NodeOptions options;

  private ConfigUtilities(
      @NonNull FileCodec fileCodec,
      @NonNull String configFile,
      @NonNull String configVersionName,
      @Nullable Changes changes,
      @NonNull Set<String> copyDirectly,
      @NonNull Placeholders placeholders,
      @Nullable Validations validations,
      @Nullable Object postInitializeCallbackArgument,
      boolean saveConfigAutomatically
  ) {
    this.fileCodec = Objects.requireNonNull(fileCodec);
    this.configFile = Objects.requireNonNull(configFile);
    this.configVersionName = Objects.requireNonNull(configVersionName);
    this.changes = changes != null ? changes : Changes.builder().build();
    this.copyDirectly = Objects.requireNonNull(copyDirectly);
    Objects.requireNonNull(placeholders);
    this.postInitializeCallbackArgument = postInitializeCallbackArgument;
    this.saveConfigAutomatically = saveConfigAutomatically;

    this.codecs = RegisteredCodecs.defaults();
    this.options = NodeOptions.builder()
        .placeholders(placeholders)
        .validations(validations)
        .build();
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Short version of {@link #createAndMapOrUpdateAndMap(Class)}
   *
   * @see #createAndMapOrUpdateAndMap(Class)
   */
  public <T> T executeOn(Class<T> mapTo) throws Throwable {
    return createAndMapOrUpdateAndMap(mapTo);
  }

  @SuppressWarnings("ConstantConditions")
  public <T> T createAndMapOrUpdateAndMap(Class<T> mapTo) throws Throwable {
    AnnotatedType configClass = GenericTypeReflector.annotate(mapTo);
    Map<String, Object> currentConfig = currentConfig();

    if (currentConfig == null) {
      currentConfig = new HashMap<>();
    } else {
      ConfigFileUpdaterResult result = update0(configClass, currentConfig);

      if (!result.succeeded()) {
        throw result.error();
      }
      if (result.config() != null) {
        currentConfig = result.config();
      }
    }

    NodeContext context = new RootNodeContext(codecs, options, configClass);
    T config = new ConfigLoader().load(context, currentConfig, postInitializeCallbackArgument);

    if (context.changed()) {
      saveConfig(config, context);
    }
    return config;
  }

  private ConfigFileUpdaterResult update0(AnnotatedType configClass, Map<String, ?> currentConfig) {
    NodeContext context = new RootNodeContext(codecs, options, configClass);

    ConfigFileUpdaterResult result =
        new ConfigUpdater()
            .update(context, currentConfig, configVersionName, changes, copyDirectly);

    if (result.succeeded() && !result.unchanged()) {
      saveConfig(result.config());
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private void saveConfig(Object config, NodeContext context) {
    if (saveConfigAutomatically) {
      Map<String, Object> encodedConfig =
          (Map<String, Object>) context.codec().serialize(context.type(), config, context);

      encodedConfig.put(configVersionName, context.configVersion());

      saveConfig(encodedConfig);
    }
  }

  // make sure that if you call this, the config version field has been added
  private void saveConfig(Map<String, Object> config) {
    if (saveConfigAutomatically) {
      DumperOptions dumperOptions = new DumperOptions();
      dumperOptions.setDefaultFlowStyle(FlowStyle.BLOCK);
      String file = new Yaml(dumperOptions).dump(config);
      fileCodec.write(configFile, file);
    }
  }

  private Map<String, Object> currentConfig() {
    Reader reader = fileCodec.read(configFile);
    return reader != null ? new Yaml().load(reader) : null;
  }

  public static final class Builder {
    private final Placeholders placeholders = new Placeholders();
    private final Set<String> copyDirectly = new HashSet<>();

    private FileCodec fileCodec;
    private String configFile;
    private String configVersionName = "config-version";
    private Changes changes;
    private Validations validations;
    private Object postInitializeCallbackArgument;

    private boolean saveConfigAutomatically = true;

    private Builder() {
    }

    @NonNull
    public Builder fileCodec(@NonNull FileCodec fileCodec) {
      this.fileCodec = Objects.requireNonNull(fileCodec);
      return this;
    }

    @NonNull
    public Builder configFile(@NonNull String configFile) {
      this.configFile = Objects.requireNonNull(configFile);
      return this;
    }

    @NonNull
    public Builder configVersionName(@NonNull String configVersionName) {
      this.configVersionName = Objects.requireNonNull(configVersionName);
      return this;
    }

    @NonNull
    public Builder changes(@NonNull Changes changes) {
      this.changes = Objects.requireNonNull(changes);
      return this;
    }

    @NonNull
    public Builder copyDirectly(@NonNull String subcategory) {
      Objects.requireNonNull(subcategory);
      copyDirectly.add(subcategory);
      return this;
    }

    @NonNull
    public Builder definePlaceholder(@NonNull String name, @NonNull Supplier<Object> supplier) {
      Objects.requireNonNull(name, "Placeholder name shouldn't be null");
      Objects.requireNonNull(supplier, "Placeholder supplier shouldn't be null");
      placeholders.addPlaceholder(name, supplier);
      return this;
    }

    @NonNull
    public Builder definePlaceholder(@NonNull String name, @NonNull Object replacement) {
      Objects.requireNonNull(name, "Placeholder name shouldn't be null");
      Objects.requireNonNull(replacement, "Placeholder replacement shouldn't be null");
      placeholders.addPlaceholder(name, replacement);
      return this;
    }

    @NonNull
    public Builder validations(@NonNull Validations validations) {
      this.validations = Objects.requireNonNull(validations);
      return this;
    }

    @NonNull
    public Builder postInitializeCallbackArgument(@Nullable Object callbackArgument) {
      this.postInitializeCallbackArgument = callbackArgument;
      return this;
    }

    @NonNull
    public Builder saveConfigAutomatically(boolean saveConfigAutomatically) {
      this.saveConfigAutomatically = saveConfigAutomatically;
      return this;
    }

    @NonNull
    public ConfigUtilities build() {
      return new ConfigUtilities(
          fileCodec,
          configFile,
          configVersionName,
          changes,
          copyDirectly,
          placeholders,
          validations,
          postInitializeCallbackArgument,
          saveConfigAutomatically
      );
    }
  }
}
