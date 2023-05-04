package org.geysermc.configutils.processor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

public class ClassProcessor extends AbstractProcessor {
  protected final List<String> lines = new ArrayList<>();

  private final String primaryAnnotationClassName;
  private final Set<String> aliasAnnotationClassNames;
  protected Consumer<TypeElement> onFound;

  private Path outputPath;

  public ClassProcessor(
      String primaryAnnotationClassName,
      Consumer<TypeElement> onFound,
      String... aliasAnnotationClassNames
  ) {
    this.primaryAnnotationClassName = primaryAnnotationClassName;
    this.aliasAnnotationClassNames =
        Arrays.stream(aliasAnnotationClassNames)
            .collect(Collectors.toSet());
    this.aliasAnnotationClassNames.add(primaryAnnotationClassName);
    this.onFound = onFound;
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    processingEnv.getMessager().printMessage(
        Diagnostic.Kind.NOTE,
        "Initializing processor " + primaryAnnotationClassName
    );

    String outputFile = processingEnv.getOptions().get("metadataOutputFile");
    if (outputFile != null && !outputFile.isEmpty()) {
      outputPath = Paths.get(outputFile);
    }
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      if (!roundEnv.errorRaised()) {
        complete();
      }

      return false;
    }

    if (!contains(annotations, aliasAnnotationClassNames)) {
      return false;
    }

    handleNested(roundEnv.getRootElements());
    return false;
  }

  private void handleNested(Collection<? extends Element> elements) {
    for (Element element : elements) {
      switch (element.getKind()) {
        case CLASS, INTERFACE: break;
        default: continue;
      }

      TypeElement typeElement = (TypeElement) element;
      handleNested(typeElement.getEnclosedElements());

      if (!contains(element.getAnnotationMirrors(), aliasAnnotationClassNames)) {
        continue;
      }

      onFound.accept(typeElement);
    }
  }

  public boolean contains(
      Collection<? extends TypeElement> elements,
      Collection<String> classNames
  ) {
    if (elements.isEmpty()) {
      return false;
    }

    for (TypeElement element : elements) {
      for (String className : classNames) {
        if (element.getQualifiedName().contentEquals(className)) {
          return true;
        }
      }
    }

    return false;
  }

  public boolean contains(
      List<? extends AnnotationMirror> elements,
      Collection<String> classNames
  ) {
    if (elements.isEmpty()) {
      return false;
    }

    for (AnnotationMirror element : elements) {
      for (String className : classNames) {
        if (element.getAnnotationType().toString().equals(className)) {
          return true;
        }
      }
    }

    return false;
  }

  public void complete() {
    // Read existing annotation list and verify each class still has this annotation
    if (!lines.isEmpty()) {
      try (BufferedWriter writer = createWriter()) {
        for (String location : lines) {
          writer.write(location);
          writer.newLine();
        }
      } catch (IOException exception) {
        exception.printStackTrace();
      }
    } else {
      processingEnv.getMessager().printMessage(
          Diagnostic.Kind.NOTE,
          "Did not find any classes annotated with " + aliasAnnotationClassNames
      );
    }
    processingEnv.getMessager().printMessage(
        Diagnostic.Kind.NOTE,
        "Completed processing for " + primaryAnnotationClassName
    );
  }

  private BufferedWriter createWriter() throws IOException {
    if (outputPath != null) {
      processingEnv.getMessager().printMessage(
          Diagnostic.Kind.NOTE,
          "Writing " + primaryAnnotationClassName + " to " + outputPath
      );
      return Files.newBufferedWriter(outputPath);
    }

    FileObject obj = processingEnv.getFiler().createResource(
        StandardLocation.CLASS_OUTPUT,
        "",
        primaryAnnotationClassName
    );
    processingEnv.getMessager().printMessage(
        Diagnostic.Kind.NOTE,
        "Writing " + primaryAnnotationClassName + " to " + obj.toUri()
    );
    return new BufferedWriter(obj.openWriter());
  }
}
