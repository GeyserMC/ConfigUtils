package org.geysermc.configutils.format.yaml;

import java.util.Map;
import org.geysermc.configutils.format.base.FormatCodec;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.Yaml;

public class YamlCodec implements FormatCodec {
  private final Yaml yaml;

  public YamlCodec() {
    DumperOptions dumperOptions = new DumperOptions();
    dumperOptions.setDefaultScalarStyle(ScalarStyle.PLAIN);
    dumperOptions.setDefaultFlowStyle(FlowStyle.BLOCK);
    dumperOptions.setProcessComments(true);

    yaml = new Yaml(new CustomRepresenter(dumperOptions), dumperOptions);
  }

  @Override
  public Map<String, Object> decode(String content) {
    return yaml.load(content);
  }

  @Override
  public String encode(Map<Object, Object> content) {
    return yaml.dump(content);
  }
}
