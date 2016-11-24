package com.lawson;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Created by lawson on 16/7/22.
 */
public class TableInfoGenerator {

  private String qualifiedClassName;

  private Map<String, TableInfoWrapper> itemsMap = new LinkedHashMap<>();

  public TableInfoGenerator(String qualifiedClassName) {
    this.qualifiedClassName = qualifiedClassName;
  }

  public void add(TableInfoWrapper c) throws ProcessingException {
    TableInfoWrapper existing = itemsMap.get(c.getTypeElement().getQualifiedName().toString());
    if (existing != null) {
      throw new ProcessingException(c.getTypeElement(), "Conflict: The class %s already exists",
          c.getTypeElement().getQualifiedName().toString());
    }
    itemsMap.put(c.getTypeElement().getQualifiedName().toString(), c);
  }

  public void generate(Elements elementUtils, Filer filer) throws IOException {
    TypeElement superClassName = elementUtils.getTypeElement(qualifiedClassName);
    String factoryClassName = superClassName.getSimpleName() + "_TABLE";
    String packageName = elementUtils.getPackageOf(superClassName).getQualifiedName().toString();

    for (TableInfoWrapper item : itemsMap.values()) {
      List<String> fieldNames = item.getFieldNames();
      TypeSpec.Builder typeSpecBuilder =
          TypeSpec.classBuilder(factoryClassName).addModifiers(Modifier.PUBLIC, Modifier.FINAL);

      /**
       * generate field
       */
      for (String name : fieldNames) {
        String fieldName = name.toUpperCase() + "_COL";
        FieldSpec columnSpec = FieldSpec.builder(String.class, fieldName)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer("$S", name.toUpperCase())
            .build();
        typeSpecBuilder.addField(columnSpec);
      }
      /**
       * table name
       */
      String tableName = superClassName.getSimpleName().toString().toUpperCase() + "_TABLE";
      /**
       * generate table creation sql
       */
      String create_sql =
          "CREATE TABLE " + tableName + " (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,";
      for (String name : fieldNames) {
        create_sql = create_sql + name.toUpperCase() + " TEXT,";
      }
      create_sql += "UNIQUE (_id) ON CONFLICT REPLACE)";
      FieldSpec createSpec = FieldSpec.builder(String.class, "TABLE_CREATE")
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
          .initializer("$S", create_sql)
          .build();
      typeSpecBuilder.addField(createSpec);

      /**
       * generate table name
       */
      FieldSpec tableNameSpec = FieldSpec.builder(String.class, "TABLE_NAME")
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
          .initializer("$S", tableName)
          .build();
      typeSpecBuilder.addField(tableNameSpec);
      /**
       * generate drop table sql
       */
      String drop_sql = "DROP TABLE IF EXISTS " + tableName;
      FieldSpec dropSpec = FieldSpec.builder(String.class, "TABLE_DROP")
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
          .initializer("$S", drop_sql)
          .build();
      typeSpecBuilder.addField(dropSpec);
      /**
       * generate basic query sql
       */
      String base_query_sql = "SELECT * FROM " + tableName;
      FieldSpec baseQuerySpec = FieldSpec.builder(String.class, "TABLE_BASE_QUERY")
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
          .initializer("$S", base_query_sql)
          .build();
      typeSpecBuilder.addField(baseQuerySpec);
      /**
       * generate delete table sql
       */
      String delete_sql = "DELETE FROM " + tableName;
      FieldSpec deleteSpec = FieldSpec.builder(String.class, "TABLE_DELETE")
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
          .initializer("$S", delete_sql)
          .build();
      typeSpecBuilder.addField(deleteSpec);

      JavaFile.builder(packageName, typeSpecBuilder.build()).build().writeTo(filer);
    }
  }
}
