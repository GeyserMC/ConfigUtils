package org.geysermc.configutils.loader.callback;

public interface GenericPostInitializeCallback<T> {
  CallbackResult postInitialize(T predefinedArgument);
}
