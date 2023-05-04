package org.geysermc.configutils.format.base;

import java.util.Map;

public interface FormatCodec {
  Map<String, Object> decode(String content);

  String encode(Map<Object, Object> content);
}
