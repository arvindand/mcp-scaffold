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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * Main configuration for MCP Scaffold.
 *
 * @param scan package scanning configuration
 * @param filter class and method filtering configuration
 * @param descriptions description generation configuration
 * @param output output file configuration
 * @param naming tool naming configuration
 * @param readOnly read-only detection configuration
 * @param annotations annotation class configuration
 * @author Arvind Menon
 */
public record ScaffoldConfig(
    ScanConfig scan,
    FilterConfig filter,
    DescriptionConfig descriptions,
    OutputConfig output,
    NamingConfig naming,
    ReadOnlyConfig readOnly,
    AnnotationConfig annotations) {

  /** Returns a configuration with all default values. */
  public static ScaffoldConfig defaults() {
    return new ScaffoldConfig(
        ScanConfig.defaults(),
        FilterConfig.defaults(),
        DescriptionConfig.defaults(),
        OutputConfig.defaults(),
        NamingConfig.defaults(),
        ReadOnlyConfig.defaults(),
        AnnotationConfig.defaults());
  }

  /** Loads configuration from a YAML file. */
  public static ScaffoldConfig fromYaml(Path path) throws IOException {
    try (InputStream is = Files.newInputStream(path)) {
      Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
      Map<String, Object> root = yaml.load(is);

      Map<String, Object> mcp = getMap(root, "mcp");
      Map<String, Object> scaffold = getMap(mcp, "scaffold");

      return new ScaffoldConfig(
          parseScanConfig(getMap(scaffold, "scan")),
          parseFilterConfig(getMap(scaffold, "filter")),
          parseDescriptionConfig(getMap(scaffold, "descriptions")),
          parseOutputConfig(getMap(scaffold, "output")),
          parseNamingConfig(getMap(scaffold, "naming")),
          parseReadOnlyConfig(getMap(scaffold, "read-only")),
          parseAnnotationConfig(getMap(scaffold, "annotations")));
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getMap(Map<String, Object> parent, String key) {
    Object value = parent != null ? parent.get(key) : null;
    return value instanceof Map ? (Map<String, Object>) value : Map.of();
  }

  @SuppressWarnings("unchecked")
  private static List<String> getList(Map<String, Object> map, String key) {
    Object value = map != null ? map.get(key) : null;
    return value instanceof List ? (List<String>) value : List.of();
  }

  private static boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue) {
    Object value = map != null ? map.get(key) : null;
    if (value instanceof Boolean) {
      return Boolean.TRUE.equals(value);
    }
    return defaultValue;
  }

  private static String getString(Map<String, Object> map, String key, String defaultValue) {
    Object value = map != null ? map.get(key) : null;
    return value instanceof String s ? s : defaultValue;
  }

  private static ScanConfig parseScanConfig(Map<String, Object> map) {
    List<String> packages = getList(map, "packages");
    return packages.isEmpty() ? ScanConfig.defaults() : new ScanConfig(packages);
  }

  private static FilterConfig parseFilterConfig(Map<String, Object> map) {
    if (map.isEmpty()) return FilterConfig.defaults();
    return new FilterConfig(
        getList(map, "include-patterns"),
        getList(map, "exclude-patterns"),
        getList(map, "exclude-methods"));
  }

  private static DescriptionConfig parseDescriptionConfig(Map<String, Object> map) {
    if (map.isEmpty()) return DescriptionConfig.defaults();
    return new DescriptionConfig(
        getBoolean(map, "include-javadoc", true),
        getBoolean(map, "include-entity-fields", true),
        getBoolean(map, "include-enum-values", true),
        getBoolean(map, "include-constraints", true),
        getBoolean(map, "parse-method-names", true));
  }

  private static OutputConfig parseOutputConfig(Map<String, Object> map) {
    if (map.isEmpty()) return OutputConfig.defaults();
    return new OutputConfig(
        getString(map, "directory", "target/generated-sources/mcp"),
        getString(map, "package-suffix", ".mcp"),
        getString(map, "class-suffix", "McpTools"));
  }

  private static NamingConfig parseNamingConfig(Map<String, Object> map) {
    if (map.isEmpty()) return NamingConfig.defaults();
    String styleStr = getString(map, "style", "SNAKE_CASE");
    NamingStyle style =
        switch (styleStr.toUpperCase()) {
          case "CAMEL_CASE" -> NamingStyle.CAMEL_CASE;
          case "KEBAB_CASE" -> NamingStyle.KEBAB_CASE;
          default -> NamingStyle.SNAKE_CASE;
        };
    return new NamingConfig(style, getBoolean(map, "include-entity-name", true));
  }

  private static ReadOnlyConfig parseReadOnlyConfig(Map<String, Object> map) {
    if (map.isEmpty()) return ReadOnlyConfig.defaults();
    return new ReadOnlyConfig(getBoolean(map, "detect-automatically", true));
  }

  private static AnnotationConfig parseAnnotationConfig(Map<String, Object> map) {
    if (map.isEmpty()) return AnnotationConfig.defaults();
    AnnotationConfig defaults = AnnotationConfig.defaults();
    return new AnnotationConfig(
        getString(map, "tool-annotation", defaults.toolAnnotation()),
        getString(map, "param-annotation", defaults.paramAnnotation()),
        getString(map, "tool-name-attribute", defaults.toolNameAttribute()),
        getString(map, "tool-description-attribute", defaults.toolDescriptionAttribute()),
        getString(map, "param-description-attribute", defaults.paramDescriptionAttribute()),
        getString(map, "param-required-attribute", defaults.paramRequiredAttribute()));
  }
}
