package org.geysermc.configutils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.configutils.file.codec.FileCodec;
import org.geysermc.configutils.file.template.TemplateReader;
import org.geysermc.configutils.loader.ConfigLoader;
import org.geysermc.configutils.loader.validate.Validations;
import org.geysermc.configutils.parser.placeholder.Placeholders;
import org.geysermc.configutils.parser.template.TemplateParseResult;
import org.geysermc.configutils.parser.template.TemplateParser;
import org.geysermc.configutils.parser.template.action.Action;
import org.geysermc.configutils.parser.template.action.predefined.PredefinedGroup;
import org.geysermc.configutils.parser.template.action.register.RegisteredActions;
import org.geysermc.configutils.updater.ConfigUpdater;
import org.geysermc.configutils.updater.change.Changes;
import org.geysermc.configutils.updater.file.ConfigFileUpdaterResult;
import org.yaml.snakeyaml.Yaml;

public class ConfigUtilities {
  private final TemplateReader templateReader;
  private final FileCodec fileCodec;
  private final String configFile;
  private final String templateFile;
  private final String configVersionName;
  private final RegisteredActions actions;
  private final Changes changes;
  private final Set<String> copyDirectly;
  private final Placeholders placeholders;
  private final Validations validations;
  private final Object postInitializeCallbackArgument;

  private final boolean saveConfigAutomatically;

  private ConfigUtilities(
      @NonNull TemplateReader templateReader,
      @NonNull FileCodec fileCodec,
      @NonNull String configFile,
      @NonNull String templateFile,
      @NonNull String configVersionName,
      @NonNull RegisteredActions actions,
      @NonNull Changes changes,
      @NonNull Set<String> copyDirectly,
      @NonNull Placeholders placeholders,
      @NonNull Validations validations,
      @Nullable Object postInitializeCallbackArgument,
      boolean saveConfigAutomatically) {
    this.templateReader = Objects.requireNonNull(templateReader);
    this.fileCodec = Objects.requireNonNull(fileCodec);
    this.configFile = Objects.requireNonNull(configFile);
    this.templateFile = Objects.requireNonNull(templateFile);
    this.configVersionName = Objects.requireNonNull(configVersionName);
    this.actions = Objects.requireNonNull(actions);
    this.changes = Objects.requireNonNull(changes);
    this.copyDirectly = Objects.requireNonNull(copyDirectly);
    this.placeholders = Objects.requireNonNull(placeholders);
    this.validations = Objects.requireNonNull(validations);
    this.postInitializeCallbackArgument = postInitializeCallbackArgument;

    this.saveConfigAutomatically = saveConfigAutomatically;
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
    ConfigFileUpdaterResult result = createOrUpdate();
    if (!result.succeeded()) {
      throw result.error();
    }

    Map<String, Object> mappedYaml;

    String text = result.lines().stream().collect(Collectors.joining(System.lineSeparator()));
    mappedYaml = new Yaml().load(text);

    //todo use code below once YamlConfigFileUpdater can add new sections to newVersion
//    // is null when the config has just been created
//    if (result.mappedYaml() == null) {
//      String text = result.lines().stream().collect(Collectors.joining(System.lineSeparator()));
//
//      mappedYaml = new Yaml().load(text);
//    } else {
//      mappedYaml = result.mappedYaml();
//    }

    ConfigLoader loader = new ConfigLoader();
    return loader.load(mappedYaml, mapTo, validations, postInitializeCallbackArgument);
  }

  public ConfigFileUpdaterResult createOrUpdate() {
    List<String> currentConfig = readCurrentConfig();
    if (currentConfig != null) {
      return update0(currentConfig);
    }

    TemplateParseResult result = create();

    if (!result.succeeded()) {
      return ConfigFileUpdaterResult.failed(result.error());
    }

    return ConfigFileUpdaterResult.ok(
        result.templateLines(), null, Collections.emptySet(), Collections.emptyList());
  }

  public TemplateParseResult create() {
    TemplateParseResult result = parseTemplate();
    if (result.succeeded()) {
      saveConfig(result.templateLines());
    }
    return result;
  }

  public ConfigFileUpdaterResult update() {
    List<String> currentConfig = readCurrentConfig();
    Objects.requireNonNull(currentConfig, "Can't update a non-existing config");
    return update0(currentConfig);
  }

  private TemplateParseResult parseTemplate() {
    return new TemplateParser(templateReader, actions).parseTemplate(templateFile, placeholders);
  }

  private ConfigFileUpdaterResult update0(List<String> currentConfig) {
    TemplateParseResult parseResult = parseTemplate();
    if (!parseResult.succeeded()) {
      return ConfigFileUpdaterResult.failed(parseResult.error());
    }

    ConfigFileUpdaterResult result =
        new ConfigUpdater()
            .update(currentConfig, configVersionName, parseResult, changes, copyDirectly);

    if (result.succeeded() && !result.changedLines().isEmpty()) {
      saveConfig(result.lines());
    }
    return result;
  }

  private void saveConfig(List<String> lines) {
    if (saveConfigAutomatically) {
      fileCodec.write(configFile, lines);
    }
  }

  private List<String> readCurrentConfig() {
    return fileCodec.read(configFile);
  }

  public static final class Builder {
    private final RegisteredActions actions = new RegisteredActions();
    private final Placeholders placeholders = new Placeholders();
    private final Set<String> copyDirectly = new HashSet<>();

    private TemplateReader templateReader;
    private FileCodec fileCodec;
    private String configFile;
    private String templateFile;
    private String configVersionName = "config-version";
    private Changes changes;
    private Validations validations;
    private Object postInitializeCallbackArgument;

    private boolean saveConfigAutomatically = true;
    private boolean defaultActions = true;

    private Builder() {
    }

    @NonNull
    public Builder templateReader(@NonNull TemplateReader templateReader) {
      this.templateReader = Objects.requireNonNull(templateReader);
      return this;
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
    public Builder template(@NonNull String templateFile) {
      this.templateFile = Objects.requireNonNull(templateFile);
      return this;
    }

    @NonNull
    public Builder configVersionName(@NonNull String configVersionName) {
      this.configVersionName = Objects.requireNonNull(configVersionName);
      return this;
    }

    @NonNull
    public Builder registerAction(@NonNull Action action) {
      actions.registerAction(action);
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
      copyDirectly.add(subcategory + '.'); // see YamlConfigFileUpdater line 101
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
    public Builder removeDefaultActions() {
      this.defaultActions = false;
      return this;
    }

    @NonNull
    public ConfigUtilities build() {
      if (defaultActions) {
        actions.registerAction(new PredefinedGroup());
      }

      Validations notNullValidations =
          validations != null ? validations : Validations.builder().build();

      return new ConfigUtilities(
          templateReader,
          fileCodec,
          configFile,
          templateFile,
          configVersionName,
          actions,
          changes,
          copyDirectly,
          placeholders,
          notNullValidations,
          postInitializeCallbackArgument,
          saveConfigAutomatically
      );
    }
  }
}
