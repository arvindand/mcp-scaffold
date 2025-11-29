/*
 * Copyright 2025 arvindand
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.arvindand.mcpscaffold.analyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;

import io.github.arvindand.mcpscaffold.config.FilterConfig;
import io.github.arvindand.mcpscaffold.model.ComponentInfo;
import io.github.arvindand.mcpscaffold.model.ComponentType;
import io.github.arvindand.mcpscaffold.model.EntityInfo;
import io.github.arvindand.mcpscaffold.model.FieldInfo;
import io.github.arvindand.mcpscaffold.model.MethodInfo;
import io.github.arvindand.mcpscaffold.model.ParamInfo;

/**
 * Analyzes Java source files to extract component metadata.
 *
 * <p>Uses JavaParser to parse source files and extract information about repositories, services,
 * their methods, and managed entities.
 *
 * @author Arvind Menon
 */
public class SourceAnalyzer {

  private static final String JAVA_EXTENSION = ".java";
  private static final String MODEL_PACKAGE = "model";
  private static final String REPOSITORY_PACKAGE = "repository";
  private static final String SERVICE_PACKAGE = "service";
  private static final String REPOSITORY_ANNOTATION = "Repository";
  private static final String NOT_NULL_ANNOTATION = "NotNull";

  private final List<Path> sourceRoots;
  private final JavaParser parser;

  public SourceAnalyzer(List<Path> sourceRoots) {
    this.sourceRoots = List.copyOf(sourceRoots);
    this.parser = new JavaParser();
  }

  /** Analyzes all matching components in a package. */
  public List<ComponentInfo> analyzePackage(String packageName, FilterConfig filter) {
    List<ComponentInfo> components = new ArrayList<>();

    for (Path sourceRoot : sourceRoots) {
      Path packagePath = sourceRoot.resolve(packageName.replace('.', '/'));
      if (Files.isDirectory(packagePath)) {
        try (Stream<Path> files = Files.list(packagePath)) {
          files
              .filter(p -> p.toString().endsWith(JAVA_EXTENSION))
              .map(p -> analyzeFile(p, filter))
              .flatMap(Optional::stream)
              .forEach(components::add);
        } catch (IOException e) {
          // Log and continue
        }
      }
    }

    return components;
  }

  /** Analyzes a single source file. */
  public Optional<ComponentInfo> analyzeFile(Path sourceFile, FilterConfig filter) {
    try {
      ParseResult<CompilationUnit> result = parser.parse(sourceFile);
      if (result.isSuccessful()) {
        return result.getResult().flatMap(cu -> analyzeCompilationUnit(cu, filter));
      }
    } catch (IOException e) {
      // Log and return empty
    }
    return Optional.empty();
  }

  private Optional<ComponentInfo> analyzeCompilationUnit(CompilationUnit cu, FilterConfig filter) {
    return cu.getPrimaryType()
        .flatMap(
            type -> {
              if (type instanceof ClassOrInterfaceDeclaration classDecl) {
                String className = classDecl.getNameAsString();

                if (!filter.matchesClass(className)) {
                  return Optional.empty();
                }

                Optional<ComponentType> componentType = detectComponentType(classDecl);
                if (componentType.isEmpty()) {
                  return Optional.empty();
                }

                String packageName =
                    cu.getPackageDeclaration().map(pd -> pd.getNameAsString()).orElse("");

                // Build import map for type resolution
                Map<String, String> importMap = buildImportMap(cu, packageName);

                List<MethodInfo> methods = extractMethods(classDecl, filter, cu, importMap);
                if (methods.isEmpty()) {
                  return Optional.empty();
                }

                Optional<String> javadoc =
                    classDecl.getJavadoc().map(jd -> jd.getDescription().toText());

                Optional<EntityInfo> managedEntity =
                    componentType.get() == ComponentType.REPOSITORY
                        ? extractManagedEntity(classDecl, cu, importMap)
                        : Optional.empty();

                return Optional.of(
                    new ComponentInfo(
                        packageName,
                        className,
                        componentType.get(),
                        javadoc,
                        methods,
                        managedEntity));
              }
              return Optional.empty();
            });
  }

  /** Builds a map from simple class names to fully qualified names based on imports. */
  private Map<String, String> buildImportMap(CompilationUnit cu, String currentPackage) {
    Map<String, String> importMap = new HashMap<>();
    addImportsFromFile(cu, importMap);
    scanModelPackages(currentPackage, importMap);
    return importMap;
  }

  private void addImportsFromFile(CompilationUnit cu, Map<String, String> importMap) {
    for (ImportDeclaration imp : cu.getImports()) {
      if (!imp.isAsterisk() && !imp.isStatic()) {
        String fullName = imp.getNameAsString();
        String simpleName = fullName.substring(fullName.lastIndexOf('.') + 1);
        importMap.put(simpleName, fullName);
      }
    }
  }

  private void scanModelPackages(String currentPackage, Map<String, String> importMap) {
    for (Path sourceRoot : sourceRoots) {
      scanModelDirectory(sourceRoot, currentPackage, importMap);
      scanParentModelDirectory(sourceRoot, currentPackage, importMap);
    }
  }

  private void scanModelDirectory(
      Path sourceRoot, String currentPackage, Map<String, String> importMap) {
    String modelPackageName = toModelPackage(currentPackage);
    Path modelPackagePath = sourceRoot.resolve(modelPackageName.replace('.', '/'));
    if (Files.isDirectory(modelPackagePath)) {
      scanJavaFilesInDirectory(modelPackagePath, modelPackageName, importMap);
    }
  }

  private void scanParentModelDirectory(
      Path sourceRoot, String currentPackage, Map<String, String> importMap) {
    String parentPackage =
        currentPackage.contains(".")
            ? currentPackage.substring(0, currentPackage.lastIndexOf('.'))
            : currentPackage;
    String parentModelPackage = parentPackage + "." + MODEL_PACKAGE;
    Path parentModelPath = sourceRoot.resolve(parentModelPackage.replace('.', '/'));
    if (Files.isDirectory(parentModelPath)) {
      scanJavaFilesInDirectory(parentModelPath, parentModelPackage, importMap);
    }
  }

  private void scanJavaFilesInDirectory(
      Path directory, String packageName, Map<String, String> importMap) {
    try (Stream<Path> files = Files.list(directory)) {
      files
          .filter(p -> p.toString().endsWith(JAVA_EXTENSION))
          .forEach(
              p -> {
                String fileName = p.getFileName().toString();
                String simpleName =
                    fileName.substring(0, fileName.length() - JAVA_EXTENSION.length());
                importMap.putIfAbsent(simpleName, packageName + "." + simpleName);
              });
    } catch (IOException e) {
      // Ignore
    }
  }

  private String toModelPackage(String packageName) {
    return packageName
        .replace(REPOSITORY_PACKAGE, MODEL_PACKAGE)
        .replace(SERVICE_PACKAGE, MODEL_PACKAGE);
  }

  /** Resolves a simple type name to fully qualified name. */
  private String resolveTypeName(
      String typeName, Map<String, String> importMap, String currentPackage) {
    // Already fully qualified
    if (typeName.contains(".")) {
      return typeName;
    }

    // Check primitives - return as-is (JavaPoet handles them)
    if (isPrimitiveType(typeName)) {
      return typeName;
    }

    // Check java.lang types
    if (isJavaLangType(typeName)) {
      return "java.lang." + typeName;
    }

    // Check common collection types
    String commonType = resolveCommonType(typeName);
    if (commonType != null) {
      return commonType;
    }

    // Check import map
    if (importMap.containsKey(typeName)) {
      return importMap.get(typeName);
    }

    // Check if it exists in current package
    if (fileExists(currentPackage, typeName)) {
      return currentPackage + "." + typeName;
    }

    // Assume it's in the model package relative to current
    String modelPackage = toModelPackage(currentPackage);
    return modelPackage + "." + typeName;
  }

  private boolean fileExists(String packageName, String simpleName) {
    for (Path sourceRoot : sourceRoots) {
      Path path =
          sourceRoot.resolve(packageName.replace('.', '/')).resolve(simpleName + JAVA_EXTENSION);
      if (Files.exists(path)) {
        return true;
      }
    }
    return false;
  }

  private boolean isPrimitiveType(String typeName) {
    return switch (typeName) {
      case "void", "boolean", "byte", "char", "short", "int", "long", "float", "double" -> true;
      default -> false;
    };
  }

  private boolean isJavaLangType(String typeName) {
    return switch (typeName) {
      case "String",
          "Integer",
          "Long",
          "Double",
          "Float",
          "Boolean",
          "Byte",
          "Short",
          "Character",
          "Object",
          "Class",
          "Void" ->
          true;
      default -> false;
    };
  }

  private String resolveCommonType(String typeName) {
    return switch (typeName) {
      case "List" -> "java.util.List";
      case "Set" -> "java.util.Set";
      case "Map" -> "java.util.Map";
      case "Optional" -> "java.util.Optional";
      case "Collection" -> "java.util.Collection";
      case "LocalDate" -> "java.time.LocalDate";
      case "LocalDateTime" -> "java.time.LocalDateTime";
      case "Page" -> "org.springframework.data.domain.Page";
      case "Slice" -> "org.springframework.data.domain.Slice";
      case "Pageable" -> "org.springframework.data.domain.Pageable";
      default -> null;
    };
  }

  private Optional<ComponentType> detectComponentType(ClassOrInterfaceDeclaration classDecl) {
    // Check for repository
    if (hasAnnotation(classDecl, REPOSITORY_ANNOTATION) || extendsRepository(classDecl)) {
      return Optional.of(ComponentType.REPOSITORY);
    }

    // Check for service
    if (hasAnnotation(classDecl, "Service")) {
      return Optional.of(ComponentType.SERVICE);
    }

    return Optional.empty();
  }

  private boolean hasAnnotation(NodeWithAnnotations<?> node, String annotationName) {
    return node.getAnnotations().stream()
        .anyMatch(
            a ->
                a.getNameAsString().equals(annotationName)
                    || a.getNameAsString().endsWith("." + annotationName));
  }

  private boolean extendsRepository(ClassOrInterfaceDeclaration classDecl) {
    return classDecl.getExtendedTypes().stream()
            .anyMatch(t -> t.getNameAsString().contains(REPOSITORY_ANNOTATION))
        || classDecl.getImplementedTypes().stream()
            .anyMatch(t -> t.getNameAsString().contains(REPOSITORY_ANNOTATION));
  }

  private List<MethodInfo> extractMethods(
      ClassOrInterfaceDeclaration classDecl,
      FilterConfig filter,
      CompilationUnit cu,
      Map<String, String> importMap) {
    String currentPackage = cu.getPackageDeclaration().map(pd -> pd.getNameAsString()).orElse("");
    return classDecl.getMethods().stream()
        .filter(m -> m.isPublic() || classDecl.isInterface())
        .filter(m -> !filter.isMethodExcluded(m.getNameAsString()))
        .map(m -> extractMethodInfo(m, importMap, currentPackage))
        .toList();
  }

  private MethodInfo extractMethodInfo(
      MethodDeclaration method, Map<String, String> importMap, String currentPackage) {
    String name = method.getNameAsString();
    Optional<Javadoc> javadocObj = method.getJavadoc();
    Optional<String> javadocText = javadocObj.map(jd -> jd.getDescription().toText());

    Map<String, String> paramDescriptions = new HashMap<>();
    javadocObj.ifPresent(
        jd ->
            jd.getBlockTags().stream()
                .filter(t -> t.getType() == JavadocBlockTag.Type.PARAM)
                .forEach(
                    t ->
                        t.getName()
                            .ifPresent(n -> paramDescriptions.put(n, t.getContent().toText()))));

    String returnType = resolveFullType(method.getType(), importMap, currentPackage);
    List<ParamInfo> parameters =
        method.getParameters().stream()
            .map(
                p ->
                    extractParamInfo(
                        p, importMap, currentPackage, paramDescriptions.get(p.getNameAsString())))
            .toList();
    List<String> annotations =
        method.getAnnotations().stream().map(a -> a.getNameAsString()).toList();

    // Read-only detection will be done separately
    return new MethodInfo(name, javadocText, returnType, parameters, false, annotations);
  }

  private ParamInfo extractParamInfo(
      Parameter param,
      Map<String, String> importMap,
      String currentPackage,
      String javadocDescription) {
    String name = param.getNameAsString();
    String type = resolveFullType(param.getType(), importMap, currentPackage);
    boolean nullable =
        !hasAnnotation(param, NOT_NULL_ANNOTATION) && !hasAnnotation(param, "NonNull");

    List<String> constraints =
        param.getAnnotations().stream()
            .filter(a -> isConstraintAnnotation(a.getNameAsString()))
            .map(Object::toString)
            .toList();

    List<String> enumValues =
        resolveEnumValues(param.getType().asString(), importMap, currentPackage);

    return new ParamInfo(
        name, type, nullable, constraints, Optional.ofNullable(javadocDescription), enumValues);
  }

  private List<String> resolveEnumValues(
      String typeName, Map<String, String> importMap, String currentPackage) {
    String resolvedName = resolveTypeName(typeName, importMap, currentPackage);

    // If it's a java.* type, we skip it
    if (resolvedName.startsWith("java.")) {
      return List.of();
    }

    String simpleName =
        resolvedName.contains(".")
            ? resolvedName.substring(resolvedName.lastIndexOf('.') + 1)
            : resolvedName;
    String packageName =
        resolvedName.contains(".") ? resolvedName.substring(0, resolvedName.lastIndexOf('.')) : "";

    for (Path sourceRoot : sourceRoots) {
      Path filePath =
          sourceRoot.resolve(packageName.replace('.', '/')).resolve(simpleName + JAVA_EXTENSION);
      if (Files.exists(filePath)) {
        try {
          ParseResult<CompilationUnit> result = parser.parse(filePath);
          if (result.isSuccessful()) {
            return result
                .getResult()
                .flatMap(cu -> cu.getEnumByName(simpleName))
                .map(
                    enumDecl ->
                        enumDecl.getEntries().stream()
                            .map(entry -> entry.getNameAsString())
                            .toList())
                .orElse(List.of());
          }
        } catch (IOException e) {
          // Ignore
        }
      }
    }
    return List.of();
  }

  /** Resolves a JavaParser Type to a fully qualified type string. */
  private String resolveFullType(Type type, Map<String, String> importMap, String currentPackage) {
    String typeStr = type.asString();

    // Handle generic types like List<Owner> or Map<PetType, Long>
    if (typeStr.contains("<")) {
      int genericStart = typeStr.indexOf('<');
      int genericEnd = typeStr.lastIndexOf('>');
      String rawType = typeStr.substring(0, genericStart);
      String genericPart = typeStr.substring(genericStart + 1, genericEnd);

      String resolvedRaw = resolveTypeName(rawType, importMap, currentPackage);

      // Handle multiple generic parameters (e.g., Map<K, V>)
      String[] genericParams = splitGenericParams(genericPart);
      StringBuilder resolvedGenerics = new StringBuilder();
      for (int i = 0; i < genericParams.length; i++) {
        if (i > 0) resolvedGenerics.append(", ");
        resolvedGenerics.append(
            resolveTypeName(genericParams[i].trim(), importMap, currentPackage));
      }

      return resolvedRaw + "<" + resolvedGenerics + ">";
    }

    return resolveTypeName(typeStr, importMap, currentPackage);
  }

  /** Splits generic parameters while respecting nested generics. */
  private String[] splitGenericParams(String genericPart) {
    List<String> params = new ArrayList<>();
    int depth = 0;
    int start = 0;

    for (int i = 0; i < genericPart.length(); i++) {
      char c = genericPart.charAt(i);
      if (c == '<') depth++;
      else if (c == '>') depth--;
      else if (c == ',' && depth == 0) {
        params.add(genericPart.substring(start, i));
        start = i + 1;
      }
    }
    params.add(genericPart.substring(start));

    return params.toArray(String[]::new);
  }

  private boolean isConstraintAnnotation(String name) {
    if (NOT_NULL_ANNOTATION.equals(name)) {
      return true;
    }
    return switch (name) {
      case "NotBlank",
          "NotEmpty",
          "Size",
          "Min",
          "Max",
          "Pattern",
          "Email",
          "Positive",
          "Negative",
          "PositiveOrZero",
          "NegativeOrZero",
          "Past",
          "Future",
          "PastOrPresent",
          "FutureOrPresent" ->
          true;
      default -> false;
    };
  }

  private Optional<EntityInfo> extractManagedEntity(
      ClassOrInterfaceDeclaration classDecl, CompilationUnit cu, Map<String, String> importMap) {
    String currentPackage = cu.getPackageDeclaration().map(pd -> pd.getNameAsString()).orElse("");

    // Try to find the entity type from extended types like JpaRepository<User, Long>
    for (ClassOrInterfaceType extended : classDecl.getExtendedTypes()) {
      Optional<NodeList<Type>> typeArgs = extended.getTypeArguments();
      if (typeArgs.isPresent() && !typeArgs.get().isEmpty()) {
        String entityTypeName = typeArgs.get().get(0).asString();

        // Resolve the fully qualified entity name
        String resolvedEntityName = resolveTypeName(entityTypeName, importMap, currentPackage);

        // Try to find and analyze the entity class
        return findAndAnalyzeEntity(entityTypeName, resolvedEntityName, cu);
      }
    }
    return Optional.empty();
  }

  private Optional<EntityInfo> findAndAnalyzeEntity(
      String simpleEntityName, String fullyQualifiedName, CompilationUnit cu) {
    // First check if it's in the same compilation unit
    Optional<ClassOrInterfaceDeclaration> entityInSameFile = cu.getClassByName(simpleEntityName);

    if (entityInSameFile.isPresent()) {
      return Optional.of(extractEntityInfo(entityInSameFile.get(), cu));
    }

    // Extract package from fully qualified name
    String entityPackage =
        fullyQualifiedName.contains(".")
            ? fullyQualifiedName.substring(0, fullyQualifiedName.lastIndexOf('.'))
            : "";

    for (Path sourceRoot : sourceRoots) {
      Path entityPath =
          sourceRoot
              .resolve(entityPackage.replace('.', '/'))
              .resolve(simpleEntityName + JAVA_EXTENSION);
      if (Files.exists(entityPath)) {
        Optional<EntityInfo> result = parseEntityFromFile(entityPath, simpleEntityName);
        if (result.isPresent()) {
          return result;
        }
      }
    }

    // Return a minimal entity info if we can't find the source
    return Optional.of(new EntityInfo(simpleEntityName, entityPackage, List.of()));
  }

  private Optional<EntityInfo> parseEntityFromFile(Path entityPath, String simpleEntityName) {
    try {
      ParseResult<CompilationUnit> result = parser.parse(entityPath);
      if (result.isSuccessful()) {
        return result
            .getResult()
            .flatMap(
                entityCu ->
                    entityCu
                        .getClassByName(simpleEntityName)
                        .map(c -> extractEntityInfo(c, entityCu)));
      }
    } catch (IOException e) {
      // Continue searching
    }
    return Optional.empty();
  }

  private EntityInfo extractEntityInfo(
      ClassOrInterfaceDeclaration entityClass, CompilationUnit cu) {
    String className = entityClass.getNameAsString();
    String packageName = cu.getPackageDeclaration().map(pd -> pd.getNameAsString()).orElse("");

    List<FieldInfo> fields =
        entityClass.getFields().stream()
            .filter(f -> !f.isStatic())
            .flatMap(
                f -> f.getVariables().stream().map(v -> extractFieldInfo(f, v.getNameAsString())))
            .toList();

    return new EntityInfo(className, packageName, fields);
  }

  private FieldInfo extractFieldInfo(FieldDeclaration field, String name) {
    String type = field.getElementType().asString();
    boolean isId = hasAnnotation(field, "Id") || hasAnnotation(field, "EmbeddedId");
    boolean nullable =
        !hasAnnotation(field, NOT_NULL_ANNOTATION)
            && !hasAnnotation(field, "Column")
            && field.getAnnotations().stream()
                .filter(a -> a.getNameAsString().equals("Column"))
                .noneMatch(a -> a.toString().contains("nullable = false"));

    List<String> constraints =
        field.getAnnotations().stream()
            .filter(a -> isConstraintAnnotation(a.getNameAsString()))
            .map(Object::toString)
            .toList();

    return new FieldInfo(name, type, isId, nullable, constraints);
  }
}
