package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.senor14.mcptestkit.client.StdioMcpTestClient;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GeneratedToolIT {
  @Test
  void generatedToolIsDiscoveredAndCallableOverStdio() {
    var client = StdioMcpTestClient.connect(
        new String[] {"java", "-jar", System.getProperty("server.jar")},
        Map.of(), Duration.ofSeconds(45));
    try {
      assertThat(client.initialized()).isTrue();
      assertThat(client.toolNames()).containsExactly("say_hello");
      assertThat(client.callTool("say_hello", Map.of("name", "Ada")).toString())
          .contains("Hello, Ada");
    } finally {
      client.close();
    }
  }
}
