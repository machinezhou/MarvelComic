package com.lawson;

import com.google.auto.service.AutoService;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class) public class TableInfoProcesser extends AbstractProcessor {

  private Elements elementUtils;
  private Filer filer;
  private Messager messager;
  private Map<String, TableInfoGenerator> generators = new LinkedHashMap<>();

  @Override public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    elementUtils = processingEnv.getElementUtils();
    filer = processingEnv.getFiler();
    messager = processingEnv.getMessager();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    try {
      // iterate over all @TableInfo annotated elements
      for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(TableInfo.class)) {
        // Check if a class has been annotated with @TableInfo
        if (annotatedElement.getKind() != ElementKind.CLASS) {
          throw new ProcessingException(annotatedElement, "Only classes can be annotated with @%s",
              TableInfo.class.getSimpleName());
        } else {
          TableInfoWrapper wrapper = new TableInfoWrapper((TypeElement) annotatedElement);
          checkValidClass(wrapper);
          String annotatedClassFullName = wrapper.getAnnotatedClassFullName();
          TableInfoGenerator generator = generators.get(annotatedClassFullName);
          if (generator == null) {
            generator = new TableInfoGenerator(annotatedClassFullName);
            generators.put(annotatedClassFullName, generator);
          }
          generator.add(wrapper);
        }
      }

      for (TableInfoGenerator generator : generators.values()) {
        generator.generate(elementUtils, filer);
      }
      generators.clear();
    } catch (ProcessingException e) {
      error(e.getElement(), e.getMessage());
    } catch (IOException e) {
      error(null, e.getMessage());
    }
    return true;
  }

  /**
   * Checks if the annotated element observes our rules
   */
  private void checkValidClass(TableInfoWrapper wrapper) throws ProcessingException {
    TypeElement classElement = wrapper.getTypeElement();
    if (!classElement.getModifiers().contains(Modifier.PUBLIC)) {
      throw new ProcessingException(classElement, "The class %s is not public.",
          classElement.getQualifiedName().toString());
    }
    if (classElement.getModifiers().contains(Modifier.ABSTRACT)) {
      throw new ProcessingException(classElement,
          "The class %s is abstract. You can't annotate abstract classes with @%",
          classElement.getQualifiedName().toString(), TableInfo.class.getSimpleName());
    }
    TypeElement superClassElement =
        elementUtils.getTypeElement(wrapper.getAnnotatedClassFullName());
    if (superClassElement.getKind() == ElementKind.INTERFACE) {
      if (!classElement.getInterfaces().contains(superClassElement.asType())) {
        throw new ProcessingException(classElement,
            "The class %s annotated with @%s must implement the interface %s",
            classElement.getQualifiedName().toString(), TableInfo.class.getSimpleName(),
            wrapper.getAnnotatedClassFullName());
      }
    }
    for (Element enclosed : classElement.getEnclosedElements()) {
      if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
        ExecutableElement constructorElement = (ExecutableElement) enclosed;
        if (constructorElement.getParameters().size() == 0 && constructorElement.getModifiers()
            .contains(Modifier.PUBLIC)) {
          return;
        }
      }
    }
    throw new ProcessingException(classElement,
        "The class %s must provide an public empty default constructor",
        classElement.getQualifiedName().toString());
  }

  @Override public Set<String> getSupportedAnnotationTypes() {
    Set<String> types = new LinkedHashSet<>();
    types.add(TableInfo.class.getCanonicalName());
    return types;
  }

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  /**
   * Prints an error message
   *
   * @param e The element which has caused the error. Can be null
   * @param msg The error message
   */
  public void error(Element e, String msg) {
    messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
  }
}
