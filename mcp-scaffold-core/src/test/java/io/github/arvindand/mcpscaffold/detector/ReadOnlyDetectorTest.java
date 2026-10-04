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
package io.github.arvindand.mcpscaffold.detector;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.arvindand.mcpscaffold.config.ReadOnlyConfig;
import io.github.arvindand.mcpscaffold.model.ComponentInfo;
import io.github.arvindand.mcpscaffold.model.ComponentType;
import io.github.arvindand.mcpscaffold.model.MethodInfo;

class ReadOnlyDetectorTest {
  private final ReadOnlyDetector detector = new ReadOnlyDetector();

  @ParameterizedTest
  @ValueSource(
      strings = {
        "findByName",
        "existsById",
        "countByStatus",
        "findAll",
        "count",
        "getReferenceById"
      })
  void recognizesRepositoryQueries(String name) {
    MethodInfo method = method(name, "java.lang.String", List.of());
    assertThat(detector.isReadOnly(method, component(ComponentType.REPOSITORY, method))).isTrue();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "getOrCreateCart",
        "readAndDelete",
        "isAvailable",
        "save",
        "findBypassToken",
        "query"
      })
  void leavesArbitraryRepositoryMethodsUnmarked(String name) {
    MethodInfo method = method(name, "java.lang.String", List.of());
    assertThat(detector.isReadOnly(method, component(ComponentType.REPOSITORY, method))).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"getOrCreateCart", "findByName", "getCart", "count"})
  void neverInfersFromServiceNames(String name) {
    MethodInfo method = method(name, "java.lang.String", List.of());
    assertThat(detector.isReadOnly(method, component(ComponentType.SERVICE, method))).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"Modifying", "org.springframework.data.jpa.repository.Modifying"})
  void modifyingAnnotationOverridesQueryName(String annotation) {
    MethodInfo method = method("findByName", "java.lang.String", List.of(annotation));
    assertThat(detector.isReadOnly(method, component(ComponentType.REPOSITORY, method))).isFalse();
  }

  @Test
  void voidQueryCannotReceiveReadOnlyHint() {
    MethodInfo method = method("findByName", "void", List.of());
    assertThat(detector.isReadOnly(method, component(ComponentType.REPOSITORY, method))).isFalse();
  }

  @Test
  void disabledDetectionClearsAnExistingHint() {
    MethodInfo method =
        new MethodInfo(
            "findByName", Optional.empty(), "java.lang.String", List.of(), true, List.of());
    ComponentInfo component = component(ComponentType.REPOSITORY, method);
    MethodInfo result =
        new ReadOnlyDetector(new ReadOnlyConfig(false)).withReadOnlyDetection(method, component);
    assertThat(result.readOnly()).isFalse();
    assertThat(result.name()).isEqualTo(method.name());
  }

  private MethodInfo method(String name, String returnType, List<String> annotations) {
    return new MethodInfo(name, Optional.empty(), returnType, List.of(), false, annotations);
  }

  private ComponentInfo component(ComponentType type, MethodInfo method) {
    return new ComponentInfo(
        "com.example", "CartRepository", type, Optional.empty(), List.of(method), Optional.empty());
  }
}
