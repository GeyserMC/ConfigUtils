package org.geysermc.configutils.action.storage;

import org.geysermc.configutils.action.SingleAction;

/**
 * This is an addon for a {@link Storable}. Unfinished means that the storable has to be finished
 * before doing a different action.
 */
public interface Unfinished {
  String friendlyName();

  default String unfinishedMessage(SingleAction newAction) {
    if (getClass().equals(newAction.getClass())) {
      return String.format(
          "Cannot perform '%s' while another '%s' is still open",
          newAction.friendlyName(), friendlyName()
      );
    }
    return String.format(
        "Cannot perform '%s' while '%s' is still open",
        newAction.friendlyName(), friendlyName()
    );
  }
}
