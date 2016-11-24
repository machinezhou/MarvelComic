package com.lawson;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

/**
 * Created by lawson on 16/7/22.
 *
 * what we need is string list of the field in annotated class, so wrap them to one class
 */
public class TableInfoWrapper {
  private TypeElement annotatedClassElement;
  private String fullName;
  private String name;
  private List<String> fieldNames = new ArrayList<>();

  public TableInfoWrapper(TypeElement typeElement) throws IllegalArgumentException {
    this.annotatedClassElement = typeElement;
    /** get annotation of this annotated class Element */
    TableInfo annotation = typeElement.getAnnotation(TableInfo.class);
    try {
      /** if source code is complied by third party .jar, get the annotation's value */
      Class<?> annotatedClass = annotation.value();
      fullName = annotatedClass.getCanonicalName();
      name = annotatedClass.getSimpleName();
    } catch (MirroredTypeException e) {
      /** normally it will throw Exception because source code is not compiled yet */
      TypeMirror mirror = e.getTypeMirror();
      DeclaredType classTypeMirror = (DeclaredType) mirror;
      TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
      fullName = classTypeElement.getQualifiedName().toString();
      name = classTypeElement.getSimpleName().toString();
    }

    fieldNames.clear();

    for (Element element : annotatedClassElement.getEnclosedElements()) {
      if (element.getKind() == ElementKind.FIELD) {
        fieldNames.add(element.getSimpleName().toString());
      }
    }
  }

  public String getAnnotatedClassFullName() {
    return fullName;
  }

  public String getName() {
    return name;
  }

  public TypeElement getTypeElement() {
    return annotatedClassElement;
  }

  public List<String> getFieldNames() {
    return fieldNames;
  }
}
