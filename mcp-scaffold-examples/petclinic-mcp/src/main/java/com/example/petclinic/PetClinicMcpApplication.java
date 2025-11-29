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
package com.example.petclinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PetClinic MCP Server Application.
 *
 * <p>This Spring Boot application exposes PetClinic service methods as MCP tools that can be used
 * by AI assistants.
 */
@SpringBootApplication
public class PetClinicMcpApplication {

  public static void main(String[] args) {
    SpringApplication.run(PetClinicMcpApplication.class, args);
  }
}
