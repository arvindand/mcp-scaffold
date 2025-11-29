import java.io.File

// Generated file is in .mcp subpackage per output config defaults
File generatedFile = new File(basedir, "target/generated-sources/mcp/com/example/mcp/SimpleServiceMcpTools.java")

assert generatedFile.exists() : "Generated file not found at: ${generatedFile.absolutePath}"
assert generatedFile.text.contains("public class SimpleServiceMcpTools")
assert generatedFile.text.contains("public String sayHello")
assert generatedFile.text.contains("@McpToolParam")
