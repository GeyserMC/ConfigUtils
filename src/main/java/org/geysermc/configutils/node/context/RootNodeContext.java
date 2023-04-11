package org.geysermc.configutils.node.context;

import java.lang.reflect.AnnotatedType;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.configutils.node.codec.RegisteredCodecs;
import org.geysermc.configutils.node.context.option.NodeOptions;
import org.geysermc.configutils.node.meta.ConfigVersion;
import org.geysermc.configutils.util.ReflectionUtils;

public final class RootNodeContext extends NodeContext {
  private final RegisteredCodecs registeredCodecs;
  private final NodeOptions options;

  public RootNodeContext(
      @NonNull RegisteredCodecs registeredCodecs,
      @NonNull NodeOptions options,
      @NonNull AnnotatedType type
  ) {
    super(type);
    this.registeredCodecs = Objects.requireNonNull(registeredCodecs);
    this.options = Objects.requireNonNull(options);
    init();
  }

  @Override
  public RegisteredCodecs codecs() {
    return registeredCodecs;
  }

  @Override
  public NodeOptions options() {
    return options;
  }

  @Override
  public int configVersion() {
    ConfigVersion version = ReflectionUtils.findAnnotation(ConfigVersion.class, type());
    return version != null ? version.value() : 0;
  }

  @Override
  public String fullKey() {
    return "";
  }

  @Override
  public @Nullable String key() {
    return null;
  }
}
