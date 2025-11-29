# How MCP Scaffold Works

This document explains the internal architecture and processing pipeline of MCP Scaffold.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                Your Spring Boot Application                      │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              spring-ai-starter-mcp-server                 │   │
│  │         (handles MCP protocol, SSE transport)             │   │
│  └─────────────────────────┬────────────────────────────────┘   │
│                            │                                     │
│  ┌─────────────────────────▼────────────────────────────────┐   │
│  │        Generated MCP Tools (from mcp-scaffold)            │   │
│  │                                                           │   │
│  │  CustomerRepositoryMcpTools ──► CustomerRepository        │   │
│  │  OrderRepositoryMcpTools ────► OrderRepository            │   │
│  │  ReportServiceMcpTools ──────► ReportService              │   │
│  └───────────────────────────────────────────────────────────┘   │
│                            │                                     │
│  ┌─────────────────────────▼────────────────────────────────┐   │
│  │              Your Existing Code (unchanged)               │   │
│  │                                                           │   │
│  │  Controllers, Services, Repositories, Entities            │   │
│  └───────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

## Processing Pipeline

### 1. Source Analysis

The `SourceAnalyzer` uses JavaParser to:

- Parse Java source files into AST
- Detect Spring components (`@Repository`, `@Service`)
- Extract method signatures and parameters
- Extract Javadoc comments
- Resolve entity types from repository generics

### 2. Read-Only Detection

The `ReadOnlyDetector` determines if methods are read-only based on:

- Method name prefixes (`find`, `get`, `count` = read; `save`, `delete` = write)
- `@Modifying` annotation
- Return type (`void` usually indicates write)
- Spring Data query method patterns

### 3. Description Enhancement

The `DescriptionEnhancer` generates rich descriptions by:

- Parsing Javadoc comments
- Converting method names to human-readable text
- Including entity field information
- Adding enum values for enum parameters
- Including validation constraint information
- Marking read-only operations

### 4. Code Generation

The `McpToolClassGenerator` uses JavaPoet to:

- Generate `@Component` wrapper classes
- Add `@Tool` annotations with descriptions
- Add `@ToolParam` annotations for parameters
- Delegate method calls to wrapped components
- Handle Optional return types

## Key Design Decisions

### Why Build-Time Generation?

- **No runtime overhead** - Generated code is just like hand-written code
- **Full IDE support** - Code completion, type checking, navigation
- **Easy debugging** - Set breakpoints in generated code
- **Reproducible** - Same input always produces same output

### Why Wrapper Classes?

- **Non-invasive** - Your existing code stays unchanged
- **Separation of concerns** - MCP concerns isolated from business logic
- **Easy to regenerate** - Update descriptions without touching original code

### Why JavaParser + JavaPoet?

- **JavaParser** - Reliable AST parsing without compilation
- **JavaPoet** - Type-safe code generation with proper imports
- **No bytecode manipulation** - Works with source code only

## Component Detection

### Repositories

Detected when:

- Class extends `JpaRepository`, `CrudRepository`, etc.
- Class has `@Repository` annotation
- Interface in configured packages

### Services

Detected when:

- Class has `@Service` annotation
- Class in configured packages

## Method Filtering

Methods are included when:

- Public visibility (or interface methods)
- Not in exclude list (`flush`, `clear`, etc.)
- Not inherited from base interfaces (configurable)

## Tool Naming

Tool names are generated using configurable strategy:

| Style | Input | Output |
|-------|-------|--------|
| SNAKE_CASE | `UserRepository.findByEmail` | `user_find_by_email` |
| CAMEL_CASE | `UserRepository.findByEmail` | `userFindByEmail` |
| KEBAB_CASE | `UserRepository.findByEmail` | `user-find-by-email` |
