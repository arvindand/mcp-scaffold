# Getting Started with MCP Scaffold

This guide walks you through setting up MCP Scaffold in your Spring Boot project.

## Prerequisites

- Java 21 or later
- Maven 3.9+
- An existing Spring Boot application with Spring Data JPA repositories

## Step 1: Add the Plugin

Add the MCP Scaffold Maven plugin to your project's `pom.xml`:

```xml
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

## Step 2: Add Runtime Dependencies

Add Spring AI MCP server starter and the MCP annotations for the generated code:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>1.1.2</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Spring AI MCP Server (WebMVC or WebFlux) -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
    </dependency>
    
    <!-- MCP Annotations (from Spring AI Community) -->
    <dependency>
        <groupId>org.springaicommunity</groupId>
        <artifactId>mcp-annotations</artifactId>
        <version>0.8.0</version>
    </dependency>
</dependencies>
```

## Step 3: Generate Configuration

Instead of writing the configuration manually, you can let the plugin scan your project and suggest a configuration:

```bash
mvn mcp-scaffold:suggest
```

This will generate `mcp-scaffold-suggested.yaml` with:

- Detected packages containing Repositories and Services
- Suggested "High Value" components to include
- Dangerous methods (like `delete`, `drop`) excluded by default

Review the file, make any necessary adjustments, and rename it to `mcp-scaffold.yaml`:

```bash
mv mcp-scaffold-suggested.yaml mcp-scaffold.yaml
```

## Step 4: Run Generation

```bash
mvn compile
```

Generated MCP tool classes will appear in `target/generated-sources/mcp`.

## Step 5: Use Generated Tools

The generated tool classes are automatically discovered by Spring's component scanning. They will be available to Spring AI MCP server for exposing to AI clients.

## Example Project Structure

```
your-project/
├── pom.xml
├── mcp-scaffold.yaml
└── src/
    └── main/
        └── java/
            └── com/yourcompany/
                ├── model/
                │   └── User.java
                ├── repository/
                │   └── UserRepository.java
                └── service/
                    └── UserService.java
```

After running `mvn compile`:

```
your-project/
├── target/
│   └── generated-sources/
│       └── mcp/
│           └── com/yourcompany/
│               ├── repository/mcp/
│               │   └── UserRepositoryMcpTools.java
│               └── service/mcp/
│                   └── UserServiceMcpTools.java
```

## Next Steps

- See [Configuration Reference](configuration.md) for all available options
- Check out the [Petclinic Example](../mcp-scaffold-examples/petclinic-mcp/) for a complete working example
