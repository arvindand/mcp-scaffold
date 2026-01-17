# MCP Scaffold

[![CI](https://github.com/arvindand/mcp-scaffold/actions/workflows/ci.yml/badge.svg)](https://github.com/arvindand/mcp-scaffold/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.arvindand/mcp-scaffold-maven-plugin.svg)](https://central.sonatype.com/artifact/io.github.arvindand/mcp-scaffold-maven-plugin)
[![Java 21](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/projects/jdk/21/)

## Turn your Spring Data repositories into AI-ready MCP tools

MCP Scaffold analyzes your existing Spring Boot application and generates `@McpTool`-annotated wrapper classes that expose your repositories and services to AI assistants via the [Model Context Protocol](https://modelcontextprotocol.io/).

## Documentation

| Guide | Description |
|-------|-------------|
| [Getting Started](docs/getting-started.md) | Step-by-step setup guide |
| [Configuration Reference](docs/configuration.md) | All YAML options explained |
| [How It Works](docs/how-it-works.md) | Architecture and internals |

## Why MCP Scaffold?

- **Zero manual annotation** — Works with your existing code, no changes required
- **Smart descriptions** — Auto-generated from Javadoc, method names, and entity metadata
- **Read-only detection** — Automatically identifies safe operations
- **Regeneratable** — Run again whenever your code changes

## Who Should Use This

MCP Scaffold is designed for:

- **Spring Boot + Spring Data JPA applications** wanting to expose data to AI assistants
- **Teams with existing repositories** that want quick MCP tool generation
- **Read-heavy query workloads** where AI assistants need database access

**Not currently supported:**

- MongoDB, R2DBC, or other non-JPA data layers
- Gradle projects (Maven only)
- Custom business logic synthesis

## Quick Start

### 1. Add the plugin and dependencies

```xml
<dependencies>
    <!-- MCP Annotations (required for generated code) -->
    <dependency>
        <groupId>org.springaicommunity</groupId>
        <artifactId>mcp-annotations</artifactId>
        <version>0.8.0</version>
    </dependency>
    
    <!-- Spring AI MCP Server -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>io.github.arvindand</groupId>
            <artifactId>mcp-scaffold-maven-plugin</artifactId>
            <version>0.1.3</version>
            <executions>
                <execution>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### 2. Generate configuration

```bash
mvn mcp-scaffold:suggest
mv mcp-scaffold-suggested.yaml mcp-scaffold.yaml
```

### 3. Build

```bash
mvn compile
```

Your MCP tools are generated in `target/generated-sources/mcp` — ready for AI assistants to use!

**[Full setup guide →](docs/getting-started.md)**

## What Gets Generated?

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
public class OwnerRepositoryMcpTools {

    @McpTool(
        name = "owner_find_by_last_name_containing_ignore_case",
        description = "Find owners by last name (case-insensitive, partial match). " +
                      "Returns Owner with: id, firstName, lastName, address, city, telephone. [Read-only]"
    )
    public List<Owner> findByLastNameContainingIgnoreCase(
        @McpToolParam(description = "The last name to search for", required = true) 
        String lastName
    ) {
        return ownerRepository.findByLastNameContainingIgnoreCase(lastName);
    }
}
```

The generated descriptions include:

- Javadoc comments
- Entity field information  
- Enum values for enum parameters
- Read-only markers

**[See the petclinic example →](mcp-scaffold-examples/petclinic-mcp/)**

## Configuration

Create `mcp-scaffold.yaml` in your project root (or use `mvn mcp-scaffold:suggest` to generate one):

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
      exclude-methods:
        - flush
        - deleteAll
    descriptions:
      include-javadoc: true
      include-entity-fields: true
      include-enum-values: true
```

**[Full configuration reference →](docs/configuration.md)**

## Try It Out

The [Petclinic example](mcp-scaffold-examples/petclinic-mcp/) is a complete working demo you can run:

```bash
./mvnw clean install
./mvnw -pl mcp-scaffold-examples/petclinic-mcp spring-boot:run
```

Then connect Claude Desktop or any MCP client to `http://localhost:8080/mcp`.

**[Full example with testing instructions →](mcp-scaffold-examples/petclinic-mcp/README.md)**

## Requirements

| Component | Version | Notes |
|-----------|---------|-------|
| Java | 21+ | Required |
| Maven | 3.9+ | Gradle not yet supported |
| Spring Boot | 3.x | Tested with 3.2+ |
| Spring AI | 1.1.2+ | Uses MCP server starter |
| mcp-annotations | 0.8.0 | From spring-ai-community |

## Current Limitations

- **Maven only** — Gradle plugin support is planned
- **JPA repositories only** — MongoDB, R2DBC, custom DAOs not supported
- **No custom method synthesis** — Only wraps existing repository/service methods
- **Java 21+ only** — No Java 17 support currently

## Building from Source

```bash
git clone https://github.com/arvindand/mcp-scaffold.git
cd mcp-scaffold
./mvnw clean install
```

## Security Considerations

**Important:** Generated MCP tools provide direct access to your data layer.

- **No built-in authentication** — Tools bypass Spring Security by default
- **Full read/write access** — AI assistants can call any exposed repository method
- **Use `exclude-methods`** — Filter dangerous operations like `deleteAll`, `save`, `flush`

For production use:

- Only expose read-only repositories to AI
- Use the `filter.exclude-methods` config to block write operations
- Consider wrapping the MCP server with authentication middleware

## License

Apache License 2.0 — see [LICENSE](LICENSE) for details.

---

**Author:** [Arvind Menon](https://github.com/arvindand)
