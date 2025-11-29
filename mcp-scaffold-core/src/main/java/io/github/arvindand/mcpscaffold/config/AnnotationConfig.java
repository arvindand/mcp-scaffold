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
 * Configuration for MCP tool annotations.
 *
 * <p>Allows customization of which annotation classes are used for the generated MCP tools. This
 * provides flexibility to adapt to annotation package changes or use alternative annotation
 * libraries.
 *
 * @param toolAnnotation fully qualified class name of the tool annotation (e.g.,
 *     "org.springaicommunity.mcp.annotation.McpTool")
 * @param paramAnnotation fully qualified class name of the parameter annotation (e.g.,
 *     "org.springaicommunity.mcp.annotation.McpToolParam")
 * @param toolNameAttribute attribute name for the tool name (e.g., "name")
 * @param toolDescriptionAttribute attribute name for the tool description (e.g., "description")
 * @param paramDescriptionAttribute attribute name for the parameter description (e.g.,
 *     "description")
 * @param paramRequiredAttribute attribute name for the required flag (e.g., "required")
 * @author Arvind Menon
 */
public record AnnotationConfig(
    String toolAnnotation,
    String paramAnnotation,
    String toolNameAttribute,
    String toolDescriptionAttribute,
    String paramDescriptionAttribute,
    String paramRequiredAttribute) {

  /** Default annotation configuration using Spring AI Community MCP annotations. */
  public static AnnotationConfig defaults() {
    return new AnnotationConfig(
        "org.springaicommunity.mcp.annotation.McpTool",
        "org.springaicommunity.mcp.annotation.McpToolParam",
        "name",
        "description",
        "description",
        "required");
  }

  /**
   * Returns the package name of the tool annotation.
   *
   * @return the package name
   */
  public String toolAnnotationPackage() {
    int lastDot = toolAnnotation.lastIndexOf('.');
    return lastDot > 0 ? toolAnnotation.substring(0, lastDot) : "";
  }

  /**
   * Returns the simple class name of the tool annotation.
   *
   * @return the simple class name
   */
  public String toolAnnotationSimpleName() {
    int lastDot = toolAnnotation.lastIndexOf('.');
    return lastDot > 0 ? toolAnnotation.substring(lastDot + 1) : toolAnnotation;
  }

  /**
   * Returns the package name of the param annotation.
   *
   * @return the package name
   */
  public String paramAnnotationPackage() {
    int lastDot = paramAnnotation.lastIndexOf('.');
    return lastDot > 0 ? paramAnnotation.substring(0, lastDot) : "";
  }

  /**
   * Returns the simple class name of the param annotation.
   *
   * @return the simple class name
   */
  public String paramAnnotationSimpleName() {
    int lastDot = paramAnnotation.lastIndexOf('.');
    return lastDot > 0 ? paramAnnotation.substring(lastDot + 1) : paramAnnotation;
  }
}
