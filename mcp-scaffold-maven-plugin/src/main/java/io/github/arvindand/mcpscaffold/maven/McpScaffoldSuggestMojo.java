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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import io.github.arvindand.mcpscaffold.analyzer.SourceAnalyzer;
import io.github.arvindand.mcpscaffold.config.FilterConfig;
import io.github.arvindand.mcpscaffold.detector.ReadOnlyDetector;
import io.github.arvindand.mcpscaffold.model.ComponentInfo;
import io.github.arvindand.mcpscaffold.model.MethodInfo;

/**
 * Maven plugin goal that scans the project and suggests an mcp-scaffold.yaml configuration.
 *
 * <p>Usage:
 *
 * <pre>
 * mvn mcp-scaffold:suggest
 * </pre>
 *
 * @author Arvind Menon
 */
@Mojo(name = "suggest", requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class McpScaffoldSuggestMojo extends AbstractMojo {

  /** The Maven project. */
  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  /** Output file for the suggested configuration. */
  @Parameter(
      property = "mcp.scaffold.suggestFile",
      defaultValue = "${project.basedir}/mcp-scaffold-suggested.yaml")
  private File suggestFile;

  /** Whether to overwrite the existing configuration file if it exists. */
  @Parameter(property = "mcp.scaffold.overwrite", defaultValue = "false")
  private boolean overwrite;

  /** Whether to include repositories in the suggestion. */
  @Parameter(property = "mcp.scaffold.includeRepositories", defaultValue = "true")
  private boolean includeRepositories;

  /** Whether to include services in the suggestion. */
  @Parameter(property = "mcp.scaffold.includeServices", defaultValue = "true")
  private boolean includeServices;

  private static final String YAML_LIST_ITEM_PREFIX = "        - ";

  @Override
  public void execute() throws MojoExecutionException {
    if (suggestFile.exists() && !overwrite) {
      getLog().warn("Suggestion file already exists: " + suggestFile);
      getLog().warn("Use -Dmcp.scaffold.overwrite=true to overwrite it.");
      return;
    }

    List<Path> sourceRoots = project.getCompileSourceRoots().stream().map(Path::of).toList();

    if (sourceRoots.isEmpty()) {
      getLog().warn("No source roots found in project");
      return;
    }

    getLog().info("Scanning source roots for suggestions: " + sourceRoots);

    // 1. Discover packages
    Set<String> packages = discoverPackages(sourceRoots);
    if (packages.isEmpty()) {
      getLog().warn("No Java packages found.");
      return;
    }

    // 2. Analyze components
    List<ComponentInfo> validComponents = discoverComponents(sourceRoots, packages);

    if (validComponents.isEmpty()) {
      getLog().warn("No matching Spring components or Repositories found.");
      return;
    }

    Set<String> suggestedPackages =
        validComponents.stream().map(ComponentInfo::packageName).collect(Collectors.toSet());

    List<String> suggestedIncludes =
        validComponents.stream().map(ComponentInfo::className).sorted().toList();

    // 4. Detect dangerous methods
    ReadOnlyDetector detector = new ReadOnlyDetector();
    Set<String> dangerousMethods = new HashSet<>();

    for (ComponentInfo component : validComponents) {
      for (MethodInfo method : component.methods()) {
        if (!detector.isReadOnly(method, component)) {
          dangerousMethods.add(method.name());
        }
      }
    }

    // 5. Generate YAML
    try {
      generateYaml(suggestedPackages, suggestedIncludes, dangerousMethods);
      getLog().info("Generated suggested configuration: " + suggestFile);
      getLog().info("Review this file and rename it to mcp-scaffold.yaml to use it.");
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to write suggestion file", e);
    }
  }

  private List<ComponentInfo> discoverComponents(List<Path> sourceRoots, Set<String> packages) {
    SourceAnalyzer analyzer = new SourceAnalyzer(sourceRoots);
    // Use a permissive filter to find everything
    FilterConfig permissiveFilter = new FilterConfig(List.of(), List.of(), List.of());

    List<ComponentInfo> allComponents = new ArrayList<>();
    for (String pkg : packages) {
      allComponents.addAll(analyzer.analyzePackage(pkg, permissiveFilter));
    }

    return allComponents.stream()
        .filter(c -> !c.methods().isEmpty())
        .filter(
            c -> (includeRepositories && c.isRepository()) || (includeServices && c.isService()))
        .toList();
  }

  private Set<String> discoverPackages(List<Path> sourceRoots) {
    Set<String> packages = new HashSet<>();
    for (Path root : sourceRoots) {
      if (!Files.exists(root)) continue;
      try (Stream<Path> walk = Files.walk(root)) {
        walk.filter(p -> p.toString().endsWith(".java"))
            .forEach(
                p -> {
                  Path parent = p.getParent();
                  if (parent != null) {
                    Path relative = root.relativize(parent);
                    String pkg = relative.toString().replace(File.separatorChar, '.');
                    if (!pkg.isEmpty()) {
                      packages.add(pkg);
                    }
                  }
                });
      } catch (IOException e) {
        getLog().warn("Failed to scan source root: " + root, e);
      }
    }
    return packages;
  }

  private void generateYaml(
      Set<String> packages, List<String> includes, Set<String> dangerousMethods)
      throws IOException {
    try (PrintWriter writer = new PrintWriter(new FileWriter(suggestFile))) {
      writer.println("mcp:");
      writer.println("  scaffold:");

      writer.println("    scan:");
      writer.println("      packages:");
      List<String> sortedPackages = new ArrayList<>(packages);
      Collections.sort(sortedPackages);
      for (String pkg : sortedPackages) {
        writer.println(YAML_LIST_ITEM_PREFIX + pkg);
      }
      writer.println();

      writer.println("    filter:");
      writer.println("      include-patterns:");
      for (String inc : includes) {
        writer.println(YAML_LIST_ITEM_PREFIX + inc);
      }
      writer.println();

      writer.println("      exclude-methods:");
      List<String> sortedDangerous = new ArrayList<>(dangerousMethods);
      Collections.sort(sortedDangerous);
      for (String method : sortedDangerous) {
        writer.println(YAML_LIST_ITEM_PREFIX + method);
      }
      writer.println();

      writer.println("    naming:");
      writer.println("      style: SNAKE_CASE");
      writer.println("      include-entity-name: true");
      writer.println();

      writer.println("    read-only:");
      writer.println("      detect-automatically: true");

      writer.println("    annotations:");
      writer.println("      tool-annotation: org.springaicommunity.mcp.annotation.McpTool");
      writer.println("      param-annotation: org.springaicommunity.mcp.annotation.McpToolParam");
    }
  }
}
