package org.geysermc.configutils;

import java.lang.reflect.Method;
import java.security.Key;
import org.geysermc.configutils.node.meta.ConfigSection;
import org.geysermc.configutils.node.meta.Defaults.DefaultBoolean;
import org.geysermc.configutils.node.meta.Defaults.DefaultNumeric;
import org.geysermc.configutils.node.meta.Defaults.DefaultString;
import org.geysermc.configutils.node.meta.Exclude;
import org.geysermc.configutils.node.meta.Hidden;
import org.geysermc.configutils.node.meta.Placeholder;

public class Main {
  public static void main(String[] args) throws Throwable {
//    ConfigUtilities utilities =
//        ConfigUtilities.builder()
//            .fileCodec(PathFileCodec.of(Paths.get("./")))
//            .configFile("config.yml")
//            .changes(Changes.builder()
//                .version(1, Changes.versionBuilder()
//                    .keyRenamed("playerLink.enable", "playerLink.enabled")
//                    .keyRenamed("playerLink.allowLinking", "playerLink.allowed"))
//                .version(2, Changes.versionBuilder()
//                    .keyRenamed("playerLink.useGlobalLinking", "playerLink.enableGlobalLinking"))
//                .build())
//            .definePlaceholder("metrics.uuid", UUID::randomUUID)
//            .build();
//    Config c = utilities.executeOn(Config.class);
//    System.out.println(c.debug());
    for (Method method : Config.class.getMethods()) {
      System.out.println(method.getName());
    }
  }

  public interface Config {
    @DefaultString("key.pem")
    String keyFileName();

    @DefaultString(".")
    String usernamePrefix();

    @DefaultBoolean(true)
    boolean replaceSpaces();

    @DefaultString("system")
    String defaultLocale();

    DisconnectMessages disconnect();

    PlayerLinkConfig playerLink();

    MetricsConfig metrics();

    @Hidden
    @DefaultBoolean
    boolean debug();

    @Exclude
    Key key();

    @Exclude String rawUsernamePrefix();

    @ConfigSection
    interface DisconnectMessages {
      @DefaultString("Please connect through the official Geyser")
      String invalidKey();

      @DefaultString("Expected {} arguments, got {}. Is Geyser up-to-date?")
      String invalidArgumentsLength();
    }

    @ConfigSection
    interface PlayerLinkConfig {
      @DefaultBoolean(true)
      boolean enabled();

      @DefaultBoolean
      boolean requireLink();

      @DefaultBoolean
      boolean enableOwnLinking();

      @DefaultBoolean(true)
      boolean allowed();

      @DefaultNumeric(300)
      long linkCodeTimeout();

      @DefaultString("mysql")
      String type();

      @DefaultBoolean(true)
      boolean enableGlobalLinking();
    }

    @ConfigSection
    interface MetricsConfig {
      @DefaultBoolean(true)
      boolean enabled();

      @Placeholder
      String uuid();
    }
  }
}
