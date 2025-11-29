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
package io.github.arvindand.mcpscaffold.config;

/**
 * Configuration for description generation.
 *
 * @param includeJavadoc include Javadoc comments in descriptions
 * @param includeEntityFields include entity field information in descriptions
 * @param includeEnumValues include enum values in parameter descriptions
 * @param includeConstraints include validation constraints in descriptions
 * @param parseMethodNames generate descriptions from method names when Javadoc is missing
 * @author Arvind Menon
 */
public record DescriptionConfig(
    boolean includeJavadoc,
    boolean includeEntityFields,
    boolean includeEnumValues,
    boolean includeConstraints,
    boolean parseMethodNames) {

  public static DescriptionConfig defaults() {
    return new DescriptionConfig(true, true, true, true, true);
  }

  /** Creates a minimal configuration with only method name parsing. */
  public static DescriptionConfig minimal() {
    return new DescriptionConfig(false, false, false, false, true);
  }
}
