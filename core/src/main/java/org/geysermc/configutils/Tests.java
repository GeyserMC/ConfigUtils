package org.geysermc.configutils;

import java.nio.file.Paths;
import org.geysermc.configutils.file.codec.PathFileCodec;
import org.geysermc.configutils.loader.callback.CallbackResult;
import org.geysermc.configutils.loader.callback.PostInitializeCallback;
import org.geysermc.configutils.node.meta.Comment;
import org.geysermc.configutils.node.meta.ConfigSection;
import org.geysermc.configutils.node.meta.ConfigVersion;
import org.geysermc.configutils.node.meta.Defaults.DefaultNumeric;
import org.geysermc.configutils.node.meta.Inherit;
import org.geysermc.configutils.node.meta.Placeholder;
import org.geysermc.configutils.updater.change.Changes;

public class Tests {
  public static void main(String[] args) throws Throwable {
    ConfigUtilities utilities =
        ConfigUtilities.builder()
            .fileCodec(PathFileCodec.of(Paths.get("./")))
            .definePlaceholder("y", System::currentTimeMillis)
            .definePlaceholder("inner.xyz", System::currentTimeMillis)
            .changes(Changes.builder()
                .version(1, Changes.versionBuilder()
                    .keyRenamed("inner.y", "inner.xyz"))
                .build())
            .configFile("config.yml")
            .build();

    long ctm = System.currentTimeMillis();
    Test t = utilities.executeOn(TestTwo.class);
    System.out.println(System.currentTimeMillis() - ctm);
    System.out.println(t.postInitialize().error() == null);
    System.out.println(t.x());
    t.x(3);
    System.out.println(t.x());
//    System.out.println(t.y());
//    System.out.println(t.inner().xyz());
  }

//  public static class TestOne implements Test {
//  }

  public interface TestTwo extends Test {
    @Override
    @Comment("yas")
    @Inherit(DefaultNumeric.class)
    int x();
  }

  @Inherit(ConfigVersion.class)
  public interface TestThree extends Test {}

//  @Inherit(ConfigVersion.class)
//  public static class TestThree {}
//
//  public static class TestFour extends TestThree {}

  @ConfigVersion(1)
  public interface Test extends PostInitializeCallback {
    @DefaultNumeric(5)
    @Comment("uwu")
    int x();

    int x(int x);

    @Placeholder
    String y();

    Inner inner();

    @Override
    default CallbackResult postInitialize() {
      return CallbackResult.ok();
    }
  }

  @ConfigSection
  public interface Inner {
    @Placeholder
    String xyz();
  }
}
