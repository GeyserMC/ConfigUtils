package org.geysermc.configutils.processor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class ConfigProcessor extends ClassProcessor {
  private static final String CONFIG_SECTION = "org.geysermc.configutils.node.meta.ConfigSection";
  // above annotations are possibly still ConfigSections
  private static final String CONFIG_VERSION = "org.geysermc.configutils.node.meta.ConfigVersion";
  private static final String INHERIT = "org.geysermc.configutils.node.meta.Inherit";

  private final Set<String> processed = new HashSet<>();

  public ConfigProcessor() {
    super(CONFIG_SECTION, null, CONFIG_VERSION, INHERIT);
    this.onFound = this::onFound;
  }

  public void onFound(TypeElement element) {
    String canonicalName = element.getQualifiedName().toString();
    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, canonicalName);
    if (processed.contains(canonicalName)) {
      return;
    }
    processed.add(canonicalName);

    lines.add("c:" + canonicalName);

    // add own members first, after that the inherited
    addMembers(element.getEnclosedElements());

    for (TypeMirror interfaceMirror : element.getInterfaces()) {
      Element anInterface = processingEnv.getTypeUtils().asElement(interfaceMirror);
      addMembers(anInterface.getEnclosedElements());
    }
  }

  private void addMembers(Collection<? extends Element> elements) {
    for (Element element : elements) {
      if (element.getKind() != ElementKind.METHOD) {
        continue;
      }

      ExecutableElement self = (ExecutableElement) element;

      // don't include setters e.g.
      if (!self.getParameters().isEmpty()) {
        continue;
      }

      String memberName = self.getSimpleName().toString();
      lines.add("m:" + memberName);
    }
  }
}
