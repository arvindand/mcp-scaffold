# Petclinic MCP Example

A demonstration of MCP Scaffold generating Spring AI MCP tools from a Spring Data JPA application.

## Overview

This example shows how MCP Scaffold transforms a simple Petclinic-style application with:

- `OwnerRepository` - JPA repository for pet owners
- `PetRepository` - JPA repository for pets
- `ClinicService` - Service layer with business logic

Into fully functional MCP tools that can be called by AI assistants via the Model Context Protocol.

## Running the Example

### 1. Build and Run

From the project root:

```bash
# Build the entire project (generates MCP tools)
./mvnw clean install

# Run the example application
cd mcp-scaffold-examples/petclinic-mcp
mvn spring-boot:run
```

Or run directly:

```bash
./mvnw -pl mcp-scaffold-examples/petclinic-mcp spring-boot:run
```

### 2. Verify the Server is Running

The MCP server starts on `http://localhost:8080` with streamable HTTP transport.

Check the health endpoint:

```bash
curl http://localhost:8080/actuator/health
```

## Testing the MCP Tools

### Streamable HTTP Endpoint

The MCP server exposes tools via streamable HTTP:

- **MCP Endpoint**: `http://localhost:8080/mcp`

### Using Claude Desktop

Add this to your Claude Desktop configuration (`~/Library/Application Support/Claude/claude_desktop_config.json` on macOS):

```json
{
  "mcpServers": {
    "petclinic": {
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

Restart Claude Desktop and you should see the Petclinic tools available.

### Using curl

List available tools:

```bash
# Initialize connection and list tools
curl -N http://localhost:8080/mcp
```

### Using httpie

```bash
http --stream GET http://localhost:8080/mcp
```

## Generated Tools

MCP Scaffold generates the following tools from the repositories:

### Owner Tools

| Tool Name | Description |
|-----------|-------------|
| `owner_find_by_last_name_containing_ignore_case` | Find owners by last name (case-insensitive, partial match) |
| `owner_find_by_telephone` | Find an owner by their telephone number |
| `owner_find_by_city` | Find all owners in a specific city |
| `owner_find_by_first_name_and_last_name` | Find owners with a specific first and last name |
| `owner_count_by_city` | Count owners in a specific city |
| `owner_exists_by_telephone` | Check if an owner exists with the given telephone |
| `owner_find_owners_with_pets` | Find owners who have at least one pet |

### Pet Tools

| Tool Name | Description |
|-----------|-------------|
| `pet_find_by_name` | Find pets by name |
| `pet_find_by_type` | Find all pets of a specific type |
| `pet_find_by_owner_id` | Find all pets belonging to an owner |

## H2 Console

The example uses an in-memory H2 database. Access the console at:

- **URL**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:petclinic`
- **Username**: `sa`
- **Password**: (empty)

## Project Structure

```text
petclinic-mcp/
├── src/main/java/com/example/petclinic/
│   ├── model/           # JPA entities (Owner, Pet, PetType)
│   ├── repository/      # Spring Data repositories
│   └── service/         # Service layer
├── src/main/resources/
│   └── application.yaml # Spring Boot configuration
├── mcp-scaffold.yaml    # MCP Scaffold configuration
└── target/generated-sources/mcp/  # Generated MCP tools
```

## Configuration

See `mcp-scaffold.yaml` for the scaffold configuration:

```yaml
mcp:
  scaffold:
    scan:
      packages:
        - com.example.petclinic.repository
        - com.example.petclinic.service
    naming:
      style: SNAKE_CASE
      include-entity-name: true
    read-only:
      detect-automatically: true
```

## Regenerating Tools

After modifying repositories or services, regenerate the MCP tools:

```bash
mvn compile
```

The generated classes appear in `target/generated-sources/mcp/`.
