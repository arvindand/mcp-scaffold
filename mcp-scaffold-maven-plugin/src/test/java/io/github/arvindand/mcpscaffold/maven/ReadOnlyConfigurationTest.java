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
package io.github.arvindand.mcpscaffold.maven;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.arvindand.mcpscaffold.config.ScaffoldConfig;

class ReadOnlyConfigurationTest {
  @TempDir Path tempDir;

  @Test
  void generateHonorsDetectionSettingAndRejectsServiceNameInference() throws Exception {
    MavenProject project = fixtureProject();
    Path configFile = writeConfig(true);
    Path output = tempDir.resolve("generated");
    McpScaffoldMojo mojo = new McpScaffoldMojo();
    setField(mojo, "project", project);
    setField(mojo, "configFile", configFile.toFile());
    setField(mojo, "outputDirectory", output.toFile());

    mojo.execute();
    Path repositoryOutput = output.resolve("com/example/mcp/CartRepositoryMcpTools.java");
    Path serviceOutput = output.resolve("com/example/mcp/CartServiceMcpTools.java");
    String enabledSource = Files.readString(repositoryOutput);
    assertThat(enabledSource).contains("[Read-only]");
    assertThat(Files.readString(serviceOutput)).doesNotContain("[Read-only]");

    writeConfig(false);
    assertThat(ScaffoldConfig.fromYaml(configFile).readOnly().detectAutomatically()).isFalse();
    mojo.execute();
    assertThat(Files.readString(repositoryOutput)).doesNotContain("[Read-only]");
  }

  @Test
  void suggestionsHonorAndPreserveExistingDetectionSetting() throws Exception {
    MavenProject project = fixtureProject();
    Path configFile = writeConfig(true);
    Path suggestion = tempDir.resolve("suggested.yaml");
    McpScaffoldSuggestMojo mojo = new McpScaffoldSuggestMojo();
    setField(mojo, "project", project);
    setField(mojo, "configFile", configFile.toFile());
    setField(mojo, "suggestFile", suggestion.toFile());
    setField(mojo, "overwrite", true);
    setField(mojo, "includeRepositories", true);
    setField(mojo, "includeServices", true);

    mojo.execute();
    ScaffoldConfig enabledSuggestion = ScaffoldConfig.fromYaml(suggestion);
    assertThat(enabledSuggestion.readOnly().detectAutomatically()).isTrue();
    assertThat(enabledSuggestion.filter().excludeMethods())
        .contains("getOrCreateCart")
        .doesNotContain("findByName");

    writeConfig(false);
    mojo.execute();
    ScaffoldConfig disabledSuggestion = ScaffoldConfig.fromYaml(suggestion);
    assertThat(disabledSuggestion.readOnly().detectAutomatically()).isFalse();
    assertThat(disabledSuggestion.filter().excludeMethods())
        .contains("getOrCreateCart", "findByName");
  }

  @Test
  void suggestionsUseDefaultsWhenNoExistingConfigurationIsPresent() throws Exception {
    McpScaffoldSuggestMojo mojo = new McpScaffoldSuggestMojo();
    Path suggestion = tempDir.resolve("suggested.yaml");
    setField(mojo, "project", fixtureProject());
    setField(mojo, "configFile", tempDir.resolve("missing.yaml").toFile());
    setField(mojo, "suggestFile", suggestion.toFile());
    setField(mojo, "includeRepositories", true);
    mojo.execute();
    assertThat(ScaffoldConfig.fromYaml(suggestion).readOnly().detectAutomatically()).isTrue();
  }

  private MavenProject fixtureProject() throws Exception {
    Path sources = tempDir.resolve("src");
    Path pkg = sources.resolve("com/example");
    Files.createDirectories(pkg);
    Files.writeString(
        pkg.resolve("CartRepository.java"),
        """
        package com.example;
        import org.springframework.stereotype.Repository;
        @Repository
        public interface CartRepository {
          String findByName(String name);
          String getOrCreateCart();
        }
        """);
    Files.writeString(
        pkg.resolve("CartService.java"),
        """
        package com.example;
        import org.springframework.stereotype.Service;
        @Service
        public class CartService {
          public String getOrCreateCart() { return "created"; }
        }
        """);
    MavenProject project = new MavenProject();
    project.addCompileSourceRoot(sources.toString());
    return project;
  }

  private Path writeConfig(boolean detection) throws Exception {
    Path config = tempDir.resolve("mcp-scaffold.yaml");
    Files.writeString(
        config,
        """
        mcp:
          scaffold:
            scan:
              packages: [com.example]
            read-only:
              detect-automatically: %s
        """
            .formatted(detection));
    return config;
  }

  private void setField(Object target, String name, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(name);
    field.setAccessible(true);
    field.set(target, value);
  }
}
