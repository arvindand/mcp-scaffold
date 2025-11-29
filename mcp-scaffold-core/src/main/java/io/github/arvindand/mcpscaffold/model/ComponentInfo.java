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
package io.github.arvindand.mcpscaffold.model;

import java.util.List;
import java.util.Optional;

/**
 * Discovered component metadata from source analysis.
 *
 * @param packageName package containing the component
 * @param className simple class name
 * @param type component type (REPOSITORY or SERVICE)
 * @param javadoc class-level Javadoc (if present)
 * @param methods public methods to expose as tools
 * @param managedEntity the JPA entity managed by this repository (if applicable)
 * @author Arvind Menon
 */
public record ComponentInfo(
    String packageName,
    String className,
    ComponentType type,
    Optional<String> javadoc,
    List<MethodInfo> methods,
    Optional<EntityInfo> managedEntity) {

  public ComponentInfo {
    methods = List.copyOf(methods);
  }

  /** Returns the fully qualified class name. */
  public String fullyQualifiedName() {
    return packageName + "." + className;
  }

  /** Returns true if this is a repository component. */
  public boolean isRepository() {
    return type == ComponentType.REPOSITORY;
  }

  /** Returns true if this is a service component. */
  public boolean isService() {
    return type == ComponentType.SERVICE;
  }

  /** Extracts entity name from the class name (e.g., "UserRepository" -> "User"). */
  public String extractEntityName() {
    if (className.endsWith("Repository")) {
      return className.substring(0, className.length() - "Repository".length());
    }
    if (className.endsWith("Service")) {
      return className.substring(0, className.length() - "Service".length());
    }
    return className;
  }
}
