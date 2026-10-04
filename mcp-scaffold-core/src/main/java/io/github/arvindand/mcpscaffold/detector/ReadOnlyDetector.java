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

import io.github.arvindand.mcpscaffold.config.ReadOnlyConfig;
import io.github.arvindand.mcpscaffold.model.ComponentInfo;
import io.github.arvindand.mcpscaffold.model.ComponentType;
import io.github.arvindand.mcpscaffold.model.MethodInfo;

/**
 * Infers read-only hints from Spring Data query conventions.
 *
 * <p>A hint is not a guarantee that an implementation has no side effects. Arbitrary service method
 * names are not sufficient evidence to infer read-only behavior.
 *
 * @author Arvind Menon
 */
public class ReadOnlyDetector {

  private final ReadOnlyConfig config;

  public ReadOnlyDetector() {
    this(ReadOnlyConfig.defaults());
  }

  public ReadOnlyDetector(ReadOnlyConfig config) {
    this.config = config;
  }

  /**
   * Determines if a method can receive a read-only hint.
   *
   * @param method the method to check
   * @param component the component containing the method
   * @return true if automatic detection is enabled and a repository query convention matches
   */
  public boolean isReadOnly(MethodInfo method, ComponentInfo component) {
    if (!config.detectAutomatically() || hasModifyingAnnotation(method) || method.returnsVoid()) {
      return false;
    }

    return component.type() == ComponentType.REPOSITORY && isSpringDataReadMethod(method.name());
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

  private boolean isSpringDataReadMethod(String name) {
    // Spring Data derived query patterns
    return name.matches("^(find|read|get|query|search|stream|count|exists)By[A-Z].*")
        || name.equals("findAll")
        || name.equals("count")
        || name.equals("getOne")
        || name.equals("getReferenceById");
  }
}
