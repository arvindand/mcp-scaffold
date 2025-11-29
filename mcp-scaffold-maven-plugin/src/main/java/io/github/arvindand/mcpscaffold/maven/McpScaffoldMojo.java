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
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import com.squareup.javapoet.JavaFile;

import io.github.arvindand.mcpscaffold.analyzer.SourceAnalyzer;
import io.github.arvindand.mcpscaffold.config.ScaffoldConfig;
import io.github.arvindand.mcpscaffold.detector.ReadOnlyDetector;
import io.github.arvindand.mcpscaffold.generator.McpToolClassGenerator;
import io.github.arvindand.mcpscaffold.model.ComponentInfo;
import io.github.arvindand.mcpscaffold.model.MethodInfo;

/**
 * Maven plugin goal that generates @McpTool-annotated wrapper classes from Spring Data repositories
 * and service components.
 *
 * <p>Usage in pom.xml:
 *
 * <pre>{@code
 * <plugin>
 *     <groupId>io.github.arvindand</groupId>
 *     <artifactId>mcp-scaffold-maven-plugin</artifactId>
 *     <version>0.1.0-SNAPSHOT</version>
 *     <executions>
 *         <execution>
 *             <goals>
 *                 <goal>generate</goal>
 *             </goals>
 *         </execution>
 *     </executions>
 * </plugin>
 * }</pre>
 *
 * @author Arvind Menon
 */
@Mojo(
    name = "generate",
    defaultPhase = LifecyclePhase.GENERATE_SOURCES,
    requiresDependencyResolution = ResolutionScope.COMPILE,
    threadSafe = true)
public class McpScaffoldMojo extends AbstractMojo {

  /** The Maven project. */
  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  /** Path to the mcp-scaffold.yaml configuration file. */
  @Parameter(
      property = "mcp.scaffold.configFile",
      defaultValue = "${project.basedir}/mcp-scaffold.yaml")
  private File configFile;

  /** Output directory for generated sources. */
  @Parameter(
      property = "mcp.scaffold.outputDirectory",
      defaultValue = "${project.build.directory}/generated-sources/mcp")
  private File outputDirectory;

  /** Skip generation if set to true. */
  @Parameter(property = "mcp.scaffold.skip", defaultValue = "false")
  private boolean skip;

  /** Fail the build if no components are found. */
  @Parameter(property = "mcp.scaffold.failOnEmpty", defaultValue = "false")
  private boolean failOnEmpty;

  @Override
  public void execute() throws MojoExecutionException {
    if (skip) {
      getLog().info("Skipping MCP scaffold generation");
      return;
    }

    ScaffoldConfig config = loadConfig();
    validateConfig(config);

    List<Path> sourceRoots = project.getCompileSourceRoots().stream().map(Path::of).toList();

    if (sourceRoots.isEmpty()) {
      getLog().warn("No source roots found in project");
      return;
    }

    getLog().info("Scanning source roots: " + sourceRoots);

    SourceAnalyzer analyzer = new SourceAnalyzer(sourceRoots);
    ReadOnlyDetector readOnlyDetector = new ReadOnlyDetector();
    McpToolClassGenerator generator = new McpToolClassGenerator(config);

    int totalGenerated = 0;

    for (String pkg : config.scan().packages()) {
      getLog().info("Analyzing package: " + pkg);

      List<ComponentInfo> components = analyzer.analyzePackage(pkg, config.filter());

      for (ComponentInfo component : components) {
        if (component.methods().isEmpty()) {
          getLog().debug("Skipping " + component.className() + " (no methods)");
          continue;
        }

        // Apply read-only detection
        ComponentInfo enhancedComponent = applyReadOnlyDetection(component, readOnlyDetector);

        try {
          JavaFile javaFile = generator.generate(enhancedComponent);
          javaFile.writeTo(outputDirectory.toPath());

          String targetClassName = config.output().targetClassName(component.className());
          getLog()
              .info(
                  "Generated: "
                      + targetClassName
                      + " ("
                      + component.methods().size()
                      + " methods)");
          totalGenerated++;
        } catch (IOException e) {
          throw new MojoExecutionException(
              "Failed to write generated file for " + component.className(), e);
        }
      }
    }

    if (totalGenerated == 0) {
      String message =
          "No MCP tool classes were generated. Check your configuration and source packages.";
      if (failOnEmpty) {
        throw new MojoExecutionException(message);
      } else {
        getLog().warn(message);
      }
    } else {
      // Add generated sources to compile path
      project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
      getLog().info("Generated " + totalGenerated + " MCP tool class(es) in " + outputDirectory);
    }
  }

  private ScaffoldConfig loadConfig() throws MojoExecutionException {
    if (configFile.exists()) {
      getLog().info("Loading configuration from: " + configFile);
      try {
        return ScaffoldConfig.fromYaml(configFile.toPath());
      } catch (IOException e) {
        throw new MojoExecutionException("Failed to load configuration from " + configFile, e);
      }
    } else {
      getLog().info("No configuration file found, using defaults");
      return ScaffoldConfig.defaults();
    }
  }

  private void validateConfig(ScaffoldConfig config) throws MojoExecutionException {
    if (config.scan().packages().isEmpty()) {
      throw new MojoExecutionException(
          "No packages configured for scanning. Add packages to mcp-scaffold.yaml or create the configuration file.");
    }
  }

  private ComponentInfo applyReadOnlyDetection(ComponentInfo component, ReadOnlyDetector detector) {
    List<MethodInfo> enhancedMethods =
        component.methods().stream()
            .map(method -> detector.withReadOnlyDetection(method, component))
            .toList();

    return new ComponentInfo(
        component.packageName(),
        component.className(),
        component.type(),
        component.javadoc(),
        enhancedMethods,
        component.managedEntity());
  }
}
