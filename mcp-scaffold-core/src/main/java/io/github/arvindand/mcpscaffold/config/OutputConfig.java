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
 * Configuration for generated output.
 *
 * @param directory output directory for generated sources
 * @param packageSuffix suffix to add to source package for generated classes
 * @param classSuffix suffix for generated class names
 * @author Arvind Menon
 */
public record OutputConfig(String directory, String packageSuffix, String classSuffix) {

  public static OutputConfig defaults() {
    return new OutputConfig("target/generated-sources/mcp", ".mcp", "McpTools");
  }

  /** Computes the target package name for a source package. */
  public String targetPackage(String sourcePackage) {
    return sourcePackage + packageSuffix;
  }

  /** Computes the target class name for a source class. */
  public String targetClassName(String sourceClassName) {
    return sourceClassName + classSuffix;
  }
}
