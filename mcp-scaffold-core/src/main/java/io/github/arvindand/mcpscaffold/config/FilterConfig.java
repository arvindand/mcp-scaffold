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

import java.util.List;
import java.util.regex.Pattern;

/**
 * Configuration for filtering components and methods.
 *
 * @param includePatterns glob patterns for classes to include (e.g., "*Repository")
 * @param excludePatterns glob patterns for classes to exclude (e.g., "*Internal*")
 * @param excludeMethods method names to exclude from generation
 * @author Arvind Menon
 */
public record FilterConfig(
    List<String> includePatterns, List<String> excludePatterns, List<String> excludeMethods) {

  public FilterConfig {
    includePatterns = List.copyOf(includePatterns);
    excludePatterns = List.copyOf(excludePatterns);
    excludeMethods = List.copyOf(excludeMethods);
  }

  public static FilterConfig defaults() {
    return new FilterConfig(
        List.of("*Repository", "*Service"),
        List.of("*Internal*", "*Test*"),
        List.of("flush", "clear", "saveAndFlush"));
  }

  /** Checks if a class name matches the include patterns and doesn't match exclude patterns. */
  public boolean matchesClass(String className) {
    boolean included =
        includePatterns.isEmpty()
            || includePatterns.stream().anyMatch(p -> matchGlob(p, className));
    boolean excluded = excludePatterns.stream().anyMatch(p -> matchGlob(p, className));
    return included && !excluded;
  }

  /** Checks if a method should be excluded. */
  public boolean isMethodExcluded(String methodName) {
    return excludeMethods.contains(methodName);
  }

  private boolean matchGlob(String pattern, String text) {
    String regex = pattern.replace("*", ".*");
    return Pattern.matches(regex, text);
  }
}
