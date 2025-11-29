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
 * Configuration for tool naming.
 *
 * @param style naming style for tool names
 * @param includeEntityName whether to include entity name in tool name
 * @author Arvind Menon
 */
public record NamingConfig(NamingStyle style, boolean includeEntityName) {

  public static NamingConfig defaults() {
    return new NamingConfig(NamingStyle.SNAKE_CASE, true);
  }
}
