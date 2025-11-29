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
package io.github.arvindand.mcpscaffold.enhancer;

import java.util.Optional;
import java.util.regex.Pattern;

import io.github.arvindand.mcpscaffold.config.DescriptionConfig;
import io.github.arvindand.mcpscaffold.model.ComponentInfo;
import io.github.arvindand.mcpscaffold.model.EntityInfo;
import io.github.arvindand.mcpscaffold.model.MethodInfo;
import io.github.arvindand.mcpscaffold.model.ParamInfo;

/**
 * Generates rich descriptions for MCP tools based on source metadata.
 *
 * <p>Combines information from Javadoc, method names, entity fields, and validation constraints to
 * produce useful descriptions for LLMs.
 *
 * @author Arvind Menon
 */
public class DescriptionEnhancer {

  private static final Pattern CAMEL_CASE_SPLIT =
      Pattern.compile("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");
  private static final int MAX_ENTITY_FIELDS = 6;

  private final DescriptionConfig config;

  public DescriptionEnhancer(DescriptionConfig config) {
    this.config = config;
  }

  /** Generates a tool description for a method. */
  public String generateToolDescription(ComponentInfo component, MethodInfo method) {
    var desc = new StringBuilder();

    // Primary description from Javadoc or method name
    Optional<String> javadoc = method.javadoc();
    if (config.includeJavadoc() && javadoc.isPresent()) {
      desc.append(cleanJavadoc(javadoc.get()));
    } else if (config.parseMethodNames()) {
      desc.append(parseMethodName(method.name()));
    }

    // Return type information
    if (!method.returnsVoid()) {
      desc.append(describeReturnType(method));
    }

    // Entity field information for repositories
    if (config.includeEntityFields()) {
      component
          .managedEntity()
          .ifPresent(
              entity -> {
                if (returnsEntity(method, entity)) {
                  desc.append(describeEntityFields(entity));
                }
              });
    }

    // Read-only marker
    if (method.readOnly()) {
      desc.append(" [Read-only]");
    }

    return desc.toString().trim();
  }

  /** Generates a parameter description. */
  public String generateParamDescription(ParamInfo param) {
    var desc = new StringBuilder();

    // Primary description from Javadoc or humanized name
    desc.append(param.javadocDescription().orElseGet(() -> humanize(param.name())));

    // Enum values
    if (config.includeEnumValues() && param.isEnum()) {
      desc.append(". Values: ").append(String.join(", ", param.enumValues()));
    }

    // Constraints
    if (config.includeConstraints() && !param.constraints().isEmpty()) {
      desc.append(" (").append(String.join(", ", param.constraints())).append(")");
    }

    return desc.toString();
  }

  /** Cleans Javadoc by removing tags and excessive whitespace. */
  private String cleanJavadoc(String javadoc) {
    if (javadoc == null || javadoc.isBlank()) {
      return "";
    }

    // Remove @tags
    String cleaned = javadoc.replaceAll("@\\w+.*", "").trim();

    // Normalize whitespace
    cleaned = cleaned.replaceAll("\\s+", " ");

    // Ensure it ends with a period
    if (!cleaned.isEmpty() && !cleaned.endsWith(".")) {
      cleaned += ".";
    }

    return cleaned;
  }

  /**
   * Parses a method name into a human-readable description.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>findByEmail -> "Find by email"
   *   <li>countByStatus -> "Count by status"
   *   <li>findByEmailAndStatus -> "Find by email and status"
   * </ul>
   */
  private String parseMethodName(String name) {
    // Handle Spring Data patterns
    String parsed =
        switch (name) {
          case "findAll" -> "Find all records";
          case "findById" -> "Find by ID";
          case "count" -> "Count all records";
          case "existsById" -> "Check if exists by ID";
          case "deleteById" -> "Delete by ID";
          case "deleteAll" -> "Delete all records";
          default -> {
            // Split camelCase and handle "By", "And", "Or"
            String[] parts = CAMEL_CASE_SPLIT.split(name);
            String result = String.join(" ", parts).toLowerCase();

            // Capitalize first letter
            if (!result.isEmpty()) {
              result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
            }

            // Clean up common patterns
            result =
                result.replace(" by ", " by ").replace(" and ", " and ").replace(" or ", " or ");

            yield result;
          }
        };

    return parsed + ".";
  }

  /** Describes the return type of a method. */
  private String describeReturnType(MethodInfo method) {
    String returnType = method.returnType();

    if (returnType.startsWith("List<") || returnType.startsWith("java.util.List<")) {
      return " Returns a list.";
    }
    if (returnType.startsWith("Optional<") || returnType.startsWith("java.util.Optional<")) {
      return " Returns the item if found.";
    }
    if (returnType.startsWith("Page<") || returnType.startsWith("Slice<")) {
      return " Returns paginated results.";
    }
    if (returnType.equals("long")
        || returnType.equals("Long")
        || returnType.equals("int")
        || returnType.equals("Integer")) {
      return " Returns a count.";
    }
    if (returnType.equals("boolean") || returnType.equals("Boolean")) {
      return " Returns true/false.";
    }

    return "";
  }

  /** Checks if a method returns the managed entity type. */
  private boolean returnsEntity(MethodInfo method, EntityInfo entity) {
    String returnType = method.returnType();
    String entityName = entity.className();

    return returnType.contains(entityName);
  }

  /** Describes entity fields for inclusion in tool description. */
  private String describeEntityFields(EntityInfo entity) {
    String summary = entity.fieldSummary(MAX_ENTITY_FIELDS);
    if (summary.isEmpty()) {
      return "";
    }

    boolean hasMore = entity.fields().size() > MAX_ENTITY_FIELDS;
    String suffix = hasMore ? ", ..." : "";

    return " Returns " + entity.className() + " with: " + summary + suffix + ".";
  }

  /** Converts a parameter name to human-readable form. */
  private String humanize(String name) {
    // Split camelCase
    String[] parts = CAMEL_CASE_SPLIT.split(name);
    String result = String.join(" ", parts).toLowerCase();

    // Capitalize first letter
    if (!result.isEmpty()) {
      result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
    }

    return result;
  }
}
