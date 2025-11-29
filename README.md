# MCP Scaffold

[![CI](https://github.com/arvindand/mcp-scaffold/actions/workflows/ci.yml/badge.svg)](https://github.com/arvindand/mcp-scaffold/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java 21](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/projects/jdk/21/)

Generate Spring AI MCP tools from existing Spring Data repositories and services.

## Overview

MCP Scaffold is a Maven plugin designed to generate MCP (Model Context Protocol) tool definitions from your existing Java code.

**Current Support:**

- **Framework**: Spring Boot
- **Components**: Spring Data Repositories, @Service classes
- **Output**: Native Spring AI MCP tool definitions

It analyzes your application's AST (Abstract Syntax Tree) to produce `@McpTool`-annotated wrapper classes with rich, auto-generated descriptions derived from your Javadoc and entity relationships.

### Key Features

- **Zero manual annotation** - Works with existing code, no changes required
- **Auto-configuration** - Scans your project to suggest the best setup
- **Smart descriptions** - Generated from Javadoc, method names, and entity metadata
- **Read-only detection** - Automatic for Spring Data methods
- **Regeneratable** - Run again whenever your code changes
- **Native Spring AI output** - Works with the framework, not against it

## Quick Start

### 1. Add the plugin to your `pom.xml`

```xml
<plugin>
    <groupId>io.github.arvindand</groupId>
    <artifactId>mcp-scaffold-maven-plugin</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### 2. Generate Configuration

Run the suggest goal to automatically discover your repositories and services:

```bash
mvn mcp-scaffold:suggest
```

You can also filter the suggestions:

```bash
# Only include repositories
mvn mcp-scaffold:suggest -Dmcp.scaffold.includeServices=false

# Only include services
mvn mcp-scaffold:suggest -Dmcp.scaffold.includeRepositories=false
```

This creates `mcp-scaffold-suggested.yaml`. Review it and rename to `mcp-scaffold.yaml`.

### 3. Run Maven

```bash
mvn compile
```

Your MCP tools are generated in `target/generated-sources/mcp`.

## How It Works

```text
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Your Source    │     │  mcp-scaffold   │     │   Generated     │
│  Code           │────►│  Maven Plugin   │────►│   MCP Tools     │
│                 │     │                 │     │                 │
│ - Repositories  │     │ - Analyzes AST  │     │ - @McpTool      │
│ - Services      │     │ - Extracts docs │     │ - Descriptions  │
│ - Entities      │     │ - Detects R/O   │     │ - Ready to use  │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

## Generated Output Example

Given this repository:

```java
@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {

    /**
     * Find owners by last name (case-insensitive, partial match).
     */
    List<Owner> findByLastNameContainingIgnoreCase(String lastName);
}
```

MCP Scaffold generates:

```java
@Component
@Generated("io.github.arvindand.mcpscaffold")
public class OwnerRepositoryMcpTools {

    private final OwnerRepository ownerRepository;

    public OwnerRepositoryMcpTools(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @McpTool(
        name = "owner_find_by_last_name_containing_ignore_case",
        description = "Find owners by last name (case-insensitive, partial match). " +
                      "Returns Owner with: id (Long), firstName (String), lastName (String), " +
                      "address (String), city (String), telephone (String). [Read-only]"
    )
    public List<Owner> findByLastNameContainingIgnoreCase(
        @McpToolParam(description = "Last name", required = true) String lastName
    ) {
        return ownerRepository.findByLastNameContainingIgnoreCase(lastName);
    }
}
```

## Configuration Reference

### Full `mcp-scaffold.yaml` Example

```yaml
mcp:
  scaffold:
    scan:
      packages:
        - com.example.repository
        - com.example.service

    filter:
      include-patterns:
        - "*Repository"
        - "*Service"
      exclude-patterns:
        - "*Internal*"
        - "*Test*"
      exclude-methods:
        - flush
        - clear
        - saveAndFlush

    descriptions:
      include-javadoc: true
      include-entity-fields: true
      include-enum-values: true
      include-constraints: true
      parse-method-names: true

    output:
      directory: target/generated-sources/mcp
      package-suffix: .mcp
      class-suffix: McpTools

    naming:
      style: SNAKE_CASE  # SNAKE_CASE, CAMEL_CASE, KEBAB_CASE
      include-entity-name: true

    read-only:
      detect-automatically: true

    # Annotation configuration (optional - defaults to Spring AI Community MCP annotations)
    # annotations:
    #   tool-annotation: org.springaicommunity.mcp.annotation.McpTool
    #   param-annotation: org.springaicommunity.mcp.annotation.McpToolParam
```

## Module Structure

```text
mcp-scaffold/
├── mcp-scaffold-core/           # Core analysis and generation logic
├── mcp-scaffold-maven-plugin/   # Maven plugin wrapper
└── mcp-scaffold-examples/       # Demo projects
    └── petclinic-mcp/          # Petclinic example
```

## Requirements

- **Java 21** or later
- **Maven 3.9+**
- **Spring Boot 3.x** application (for generated code)
- **Spring AI 1.1.0+** with MCP server starter
- **MCP Annotations** from Spring AI Community:

```xml
<dependency>
    <groupId>org.springaicommunity</groupId>
    <artifactId>mcp-annotations</artifactId>
    <version>0.7.0</version>
</dependency>
```

## Building from Source

```bash
git clone https://github.com/arvindand/mcp-scaffold.git
cd mcp-scaffold
./mvnw clean install
```

## Code Formatting

This project uses **Spotless** with **Google Java Format** for automated code formatting:

```bash
# Format all code
./mvnw spotless:apply

# Check formatting
./mvnw spotless:check
```

The formatter enforces:

- Google Java Format (2-space indentation)
- Import order: java, javax, jakarta, org, com
- Automatic removal of unused imports
- Consistent code style across the project

## License

Apache License 2.0 - see [LICENSE](LICENSE) for details.

## Author

Arvind Menon

- GitHub: [@arvindand](https://github.com/arvindand)
