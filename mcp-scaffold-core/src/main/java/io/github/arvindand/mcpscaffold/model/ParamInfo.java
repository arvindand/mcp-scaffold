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
 * Parameter metadata for method parameters.
 *
 * @param name parameter name
 * @param type fully qualified type name
 * @param nullable whether the parameter accepts null values
 * @param constraints validation constraints (e.g., @NotNull, @Size)
 * @param javadocDescription description from Javadoc @param tag
 * @param enumValues possible values if this is an enum type
 * @author Arvind Menon
 */
public record ParamInfo(
    String name,
    String type,
    boolean nullable,
    List<String> constraints,
    Optional<String> javadocDescription,
    List<String> enumValues) {

  public ParamInfo {
    constraints = List.copyOf(constraints);
    enumValues = List.copyOf(enumValues);
  }

  /** Creates a simple parameter with minimal information. */
  public static ParamInfo simple(String name, String type) {
    return new ParamInfo(name, type, true, List.of(), Optional.empty(), List.of());
  }

  /** Returns true if this parameter represents an enum type. */
  public boolean isEnum() {
    return !enumValues.isEmpty();
  }
}
