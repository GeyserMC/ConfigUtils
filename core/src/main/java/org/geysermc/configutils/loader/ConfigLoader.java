package org.geysermc.configutils.loader;

import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.configutils.loader.callback.CallbackResult;
import org.geysermc.configutils.loader.callback.GenericPostInitializeCallback;
import org.geysermc.configutils.loader.callback.PostInitializeCallback;
import org.geysermc.configutils.node.context.NodeContext;

public class ConfigLoader {
  @NonNull
  @SuppressWarnings("unchecked")
  public <T> T load(
      @NonNull NodeContext context,
      @NonNull Map<?, ?> data,
      @Nullable Object callbackArgument
  ) {
    T config = (T) context.codec().deserialize(context.type(), data, context);

    CallbackResult result = null;

    try_block:
    try {
      if (config instanceof GenericPostInitializeCallback) {
        //noinspection unchecked,rawtypes
        result = ((GenericPostInitializeCallback) config).postInitialize(callbackArgument);
        if (!result.success()) {
          // don't throw the exception in the try/catch block
          break try_block;
        }
      }

      if (config instanceof PostInitializeCallback) {
        result = ((PostInitializeCallback) config).postInitialize();
      }
    } catch (Exception exception) {
      throw new IllegalStateException(
          "An unknown error happened while executing a post-initialize callback", exception
      );
    }

    if (result != null && !result.success()) {
      throw result.error();
    }

    return config;
  }
}
