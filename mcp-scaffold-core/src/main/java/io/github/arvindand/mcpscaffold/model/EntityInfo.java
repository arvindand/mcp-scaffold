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
import java.util.stream.Collectors;

/**
 * JPA entity metadata.
 *
 * @param className simple class name of the entity
 * @param packageName package containing the entity
 * @param fields entity fields
 * @author Arvind Menon
 */
public record EntityInfo(String className, String packageName, List<FieldInfo> fields) {

  public EntityInfo {
    fields = List.copyOf(fields);
  }

  /** Returns the fully qualified class name. */
  public String fullyQualifiedName() {
    return packageName + "." + className;
  }

  /** Returns the ID field if present. */
  public Optional<FieldInfo> idField() {
    return fields.stream().filter(FieldInfo::id).findFirst();
  }

  /** Returns a summary of field names and types (limited to first N fields). */
  public String fieldSummary(int maxFields) {
    return fields.stream()
        .limit(maxFields)
        .map(f -> f.name() + " (" + simplifyType(f.type()) + ")")
        .collect(Collectors.joining(", "));
  }

  private String simplifyType(String type) {
    int lastDot = type.lastIndexOf('.');
    return lastDot >= 0 ? type.substring(lastDot + 1) : type;
  }
}
