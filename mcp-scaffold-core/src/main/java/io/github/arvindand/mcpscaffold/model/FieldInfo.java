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

/**
 * Field metadata for JPA entity fields.
 *
 * @param name field name
 * @param type fully qualified type name
 * @param id whether this is the primary key field
 * @param nullable whether the field is nullable
 * @param constraints validation constraints
 * @author Arvind Menon
 */
public record FieldInfo(
    String name, String type, boolean id, boolean nullable, List<String> constraints) {

  public FieldInfo {
    constraints = List.copyOf(constraints);
  }

  /** Creates a simple field with minimal information. */
  public static FieldInfo simple(String name, String type) {
    return new FieldInfo(name, type, false, true, List.of());
  }

  /** Creates an ID field. */
  public static FieldInfo id(String name, String type) {
    return new FieldInfo(name, type, true, false, List.of());
  }
}
