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
package io.github.arvindand.mcpscaffold.detector;

import java.util.Set;

import io.github.arvindand.mcpscaffold.model.ComponentInfo;
import io.github.arvindand.mcpscaffold.model.ComponentType;
import io.github.arvindand.mcpscaffold.model.MethodInfo;

/**
 * Detects whether methods are read-only operations.
 *
 * <p>This is particularly important for Spring Data repositories where read-only detection is
 * reliable based on method naming conventions.
 *
 * @author Arvind Menon
 */
public class ReadOnlyDetector {

  private static final Set<String> READ_PREFIXES =
      Set.of("find", "get", "read", "query", "search", "stream", "count", "exists", "is", "has");

  private static final Set<String> WRITE_PREFIXES =
      Set.of(
          "save", "insert", "create", "add", "persist", "update", "modify", "set", "change",
          "delete", "remove", "drop", "clear", "flush");

  /**
   * Determines if a method is read-only.
   *
   * @param method the method to check
   * @param component the component containing the method
   * @return true if the method is read-only
   */
  public boolean isReadOnly(MethodInfo method, ComponentInfo component) {
    // @Modifying annotation indicates a write operation
    if (hasModifyingAnnotation(method)) {
      return false;
    }

    String name = method.name().toLowerCase();

    // Check write prefixes first (higher priority)
    if (startsWithAny(name, WRITE_PREFIXES)) {
      return false;
    }

    // Check read prefixes
    if (startsWithAny(name, READ_PREFIXES)) {
      return true;
    }

    // void return type typically indicates a write operation
    if (method.returnsVoid()) {
      return false;
    }

    // For repositories, use Spring Data method pattern matching
    if (component.type() == ComponentType.REPOSITORY) {
      return isSpringDataReadMethod(name);
    }

    // For services, default to write (conservative)
    return false;
  }

  /** Creates a new MethodInfo with the read-only flag set based on detection. */
  public MethodInfo withReadOnlyDetection(MethodInfo method, ComponentInfo component) {
    boolean readOnly = isReadOnly(method, component);
    return new MethodInfo(
        method.name(),
        method.javadoc(),
        method.returnType(),
        method.parameters(),
        readOnly,
        method.annotations());
  }

  private boolean hasModifyingAnnotation(MethodInfo method) {
    return method.annotations().stream()
        .anyMatch(a -> a.equals("Modifying") || a.endsWith(".Modifying"));
  }

  private boolean startsWithAny(String name, Set<String> prefixes) {
    return prefixes.stream().anyMatch(name::startsWith);
  }

  private boolean isSpringDataReadMethod(String name) {
    // Spring Data derived query patterns
    return name.matches("^(find|read|get|query|search|stream|count|exists)by.*")
        || name.equals("findall")
        || name.equals("findbyid")
        || name.equals("count")
        || name.equals("existsbyid")
        || name.equals("getone")
        || name.equals("getbyid");
  }
}
