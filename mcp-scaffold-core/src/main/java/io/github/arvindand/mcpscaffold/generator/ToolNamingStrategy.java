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
package io.github.arvindand.mcpscaffold.generator;

import java.util.regex.Pattern;

import io.github.arvindand.mcpscaffold.config.NamingConfig;
import io.github.arvindand.mcpscaffold.model.ComponentInfo;
import io.github.arvindand.mcpscaffold.model.MethodInfo;

/**
 * Generates tool names according to configured naming strategy.
 *
 * @author Arvind Menon
 */
public class ToolNamingStrategy {

  private static final Pattern CAMEL_CASE_SPLIT =
      Pattern.compile("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");

  /**
   * Generates a tool name for a method.
   *
   * @param component the component containing the method
   * @param method the method to name
   * @param config naming configuration
   * @return the generated tool name
   */
  public String generate(ComponentInfo component, MethodInfo method, NamingConfig config) {
    String entityName = component.extractEntityName();
    String methodName = method.name();

    String combined = config.includeEntityName() ? entityName + "_" + methodName : methodName;

    return switch (config.style()) {
      case SNAKE_CASE -> toSnakeCase(combined);
      case CAMEL_CASE -> toCamelCase(combined);
      case KEBAB_CASE -> toKebabCase(combined);
    };
  }

  /** Converts a string to snake_case. */
  public String toSnakeCase(String input) {
    String[] parts = CAMEL_CASE_SPLIT.split(input);
    return String.join("_", parts).toLowerCase().replace("__", "_");
  }

  /** Converts a string to camelCase. */
  public String toCamelCase(String input) {
    String[] parts = CAMEL_CASE_SPLIT.split(input);
    if (parts.length == 0) return input.toLowerCase();

    var result = new StringBuilder(parts[0].toLowerCase());
    for (int i = 1; i < parts.length; i++) {
      String part = parts[i];
      if (!part.isEmpty()) {
        result.append(Character.toUpperCase(part.charAt(0)));
        if (part.length() > 1) {
          result.append(part.substring(1).toLowerCase());
        }
      }
    }
    return result.toString();
  }

  /** Converts a string to kebab-case. */
  public String toKebabCase(String input) {
    String[] parts = CAMEL_CASE_SPLIT.split(input);
    return String.join("-", parts).toLowerCase().replace("--", "-");
  }
}
