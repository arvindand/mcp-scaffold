# Configuration Reference

MCP Scaffold is configured via `mcp-scaffold.yaml` in your project root.

## Generating Configuration

You can bootstrap your configuration using the `suggest` goal:

```bash
mvn mcp-scaffold:suggest
```

This analyzes your codebase and generates a `mcp-scaffold-suggested.yaml` file with recommended settings, including:

- Package discovery
- Component whitelisting
- Dangerous method exclusion

### Filtering Suggestions

You can control which types of components are suggested using system properties:

```bash
# Only suggest Services (exclude Repositories)
mvn mcp-scaffold:suggest -Dmcp.scaffold.includeRepositories=false

# Only suggest Repositories (exclude Services)
mvn mcp-scaffold:suggest -Dmcp.scaffold.includeServices=false
```

## Complete Configuration

```yaml
mcp:
  scaffold:
    # Package scanning
    scan:
      packages:
        - com.example.repository
        - com.example.service

    # Class and method filtering
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

    # Description generation
    descriptions:
      include-javadoc: true
      include-entity-fields: true
      include-enum-values: true
      include-constraints: true
      parse-method-names: true

    # Output configuration
    output:
      directory: target/generated-sources/mcp
      package-suffix: .mcp
      class-suffix: McpTools

    # Tool naming
    naming:
      style: SNAKE_CASE
      include-entity-name: true

    # Read-only detection
    read-only:
      detect-automatically: true
```

## Configuration Sections

### `scan`

Controls which packages are scanned for components.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `packages` | List | `[]` | List of package names to scan |

### `filter`

Controls which classes and methods are included in generation.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `include-patterns` | List | `["*Repository", "*Service"]` | Glob patterns for classes to include |
| `exclude-patterns` | List | `["*Internal*", "*Test*"]` | Glob patterns for classes to exclude |
| `exclude-methods` | List | `["flush", "clear", "saveAndFlush"]` | Method names to exclude |

### `descriptions`

Controls how tool descriptions are generated.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `include-javadoc` | Boolean | `true` | Include Javadoc comments in descriptions |
| `include-entity-fields` | Boolean | `true` | Include entity field information for repositories |
| `include-enum-values` | Boolean | `true` | Include enum values in parameter descriptions |
| `include-constraints` | Boolean | `true` | Include validation constraints |
| `parse-method-names` | Boolean | `true` | Generate descriptions from method names |

### `output`

Controls generated file output.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `directory` | String | `target/generated-sources/mcp` | Output directory |
| `package-suffix` | String | `.mcp` | Suffix added to source package |
| `class-suffix` | String | `McpTools` | Suffix for generated class names |

### `naming`

Controls tool naming conventions.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `style` | Enum | `SNAKE_CASE` | Naming style: `SNAKE_CASE`, `CAMEL_CASE`, `KEBAB_CASE` |
| `include-entity-name` | Boolean | `true` | Include entity name in tool name |

### `read-only`

Controls read-only operation detection.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `detect-automatically` | Boolean | `true` | Auto-detect read-only methods |

### `annotations`

Configures the annotation classes used in generated code. This allows adaptation to different annotation libraries or package changes.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `tool-annotation` | String | `org.springaicommunity.mcp.annotation.McpTool` | Fully qualified class name of tool annotation |
| `param-annotation` | String | `org.springaicommunity.mcp.annotation.McpToolParam` | Fully qualified class name of parameter annotation |
| `tool-name-attribute` | String | `name` | Attribute name for tool name |
| `tool-description-attribute` | String | `description` | Attribute name for tool description |
| `param-description-attribute` | String | `description` | Attribute name for parameter description |
| `param-required-attribute` | String | `required` | Attribute name for required flag |

**Example: Using custom annotations**

```yaml
mcp:
  scaffold:
    annotations:
      tool-annotation: com.mycompany.mcp.Tool
      param-annotation: com.mycompany.mcp.ToolParam
```

## Maven Plugin Parameters

The plugin also accepts parameters directly in `pom.xml`:

```xml
<plugin>
    <groupId>io.github.arvindand</groupId>
    <artifactId>mcp-scaffold-maven-plugin</artifactId>
    <version>0.1.2</version>
    <configuration>
        <configFile>${project.basedir}/custom-config.yaml</configFile>
        <outputDirectory>${project.build.directory}/generated-sources/mcp</outputDirectory>
        <skip>false</skip>
        <failOnEmpty>false</failOnEmpty>
    </configuration>
</plugin>
```

| Parameter | Property | Default | Description |
|-----------|----------|---------|-------------|
| `configFile` | `mcp.scaffold.configFile` | `${project.basedir}/mcp-scaffold.yaml` | Configuration file path |
| `outputDirectory` | `mcp.scaffold.outputDirectory` | `${project.build.directory}/generated-sources/mcp` | Output directory |
| `skip` | `mcp.scaffold.skip` | `false` | Skip generation |
| `failOnEmpty` | `mcp.scaffold.failOnEmpty` | `false` | Fail build if no components found |
