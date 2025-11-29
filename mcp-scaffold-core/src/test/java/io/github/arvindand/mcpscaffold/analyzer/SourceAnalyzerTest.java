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
package io.github.arvindand.mcpscaffold.analyzer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.arvindand.mcpscaffold.config.FilterConfig;
import io.github.arvindand.mcpscaffold.model.ComponentInfo;
import io.github.arvindand.mcpscaffold.model.MethodInfo;
import io.github.arvindand.mcpscaffold.model.ParamInfo;

class SourceAnalyzerTest {

  @TempDir Path tempDir;

  @Test
  void shouldExtractJavadocAndEnumValues() throws IOException {
    // Create package structure
    Path packageDir = tempDir.resolve("com/example/service");
    Files.createDirectories(packageDir);

    // Create Enum
    String enumSource =
        """
        package com.example.service;

        public enum Status {
            ACTIVE, INACTIVE, PENDING
        }
        """;
    Files.writeString(packageDir.resolve("Status.java"), enumSource);

    // Create Service
    String serviceSource =
        """
        package com.example.service;

        import org.springframework.stereotype.Service;

        @Service
        public class UserService {

            /**
             * Updates the user status.
             *
             * @param userId the ID of the user
             * @param status the new status
             */
            public void updateStatus(String userId, Status status) {
            }
        }
        """;
    Files.writeString(packageDir.resolve("UserService.java"), serviceSource);

    // Analyze
    SourceAnalyzer analyzer = new SourceAnalyzer(List.of(tempDir));
    List<ComponentInfo> components =
        analyzer.analyzePackage("com.example.service", FilterConfig.defaults());

    assertEquals(1, components.size());
    ComponentInfo component = components.get(0);
    assertEquals("UserService", component.className());

    assertEquals(1, component.methods().size());
    MethodInfo method = component.methods().get(0);
    assertEquals("updateStatus", method.name());

    assertEquals(2, method.parameters().size());

    ParamInfo userIdParam = method.parameters().get(0);
    assertEquals("userId", userIdParam.name());
    assertTrue(userIdParam.javadocDescription().isPresent());
    assertEquals("the ID of the user", userIdParam.javadocDescription().get().trim());

    ParamInfo statusParam = method.parameters().get(1);
    assertEquals("status", statusParam.name());
    assertTrue(statusParam.javadocDescription().isPresent());
    assertEquals("the new status", statusParam.javadocDescription().get().trim());

    assertTrue(statusParam.isEnum());
    assertEquals(List.of("ACTIVE", "INACTIVE", "PENDING"), statusParam.enumValues());
  }
}
