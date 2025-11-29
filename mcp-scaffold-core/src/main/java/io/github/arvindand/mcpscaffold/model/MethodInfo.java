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
package io.github.arvindand.mcpscaffold.model;

import java.util.List;
import java.util.Optional;

/**
 * Method metadata extracted from source analysis.
 *
 * @param name method name
 * @param javadoc Javadoc comment (if present)
 * @param returnType fully qualified return type
 * @param parameters method parameters
 * @param readOnly whether this is a read-only operation
 * @param annotations annotation names present on the method
 * @author Arvind Menon
 */
public record MethodInfo(
    String name,
    Optional<String> javadoc,
    String returnType,
    List<ParamInfo> parameters,
    boolean readOnly,
    List<String> annotations) {

  public MethodInfo {
    parameters = List.copyOf(parameters);
    annotations = List.copyOf(annotations);
  }

  /** Returns true if this method returns void. */
  public boolean returnsVoid() {
    return "void".equals(returnType);
  }

  /** Returns true if this method returns an Optional type. */
  public boolean returnsOptional() {
    return returnType.startsWith("java.util.Optional") || returnType.startsWith("Optional");
  }

  /** Returns true if the method has no parameters. */
  public boolean hasNoParameters() {
    return parameters.isEmpty();
  }
}
