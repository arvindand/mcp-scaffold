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
package com.example.petclinic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.petclinic.model.Pet;
import com.example.petclinic.model.PetType;

/**
 * Repository for managing pets.
 *
 * <p>Provides methods for finding pets by name, type, and owner.
 *
 * @author Arvind Menon
 */
@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

  /**
   * Find pets by name (case-insensitive).
   *
   * @param name the pet name
   * @return list of matching pets
   */
  List<Pet> findByNameContainingIgnoreCase(String name);

  /**
   * Find pets by type.
   *
   * @param type the pet type
   * @return list of pets of that type
   */
  List<Pet> findByType(PetType type);

  /**
   * Find all pets belonging to an owner.
   *
   * @param ownerId the owner's ID
   * @return list of the owner's pets
   */
  List<Pet> findByOwnerId(Long ownerId);

  /**
   * Count pets by type.
   *
   * @param type the pet type
   * @return the count
   */
  long countByType(PetType type);

  /**
   * Find pets by owner's last name.
   *
   * @param lastName the owner's last name
   * @return list of pets
   */
  List<Pet> findByOwnerLastNameContainingIgnoreCase(String lastName);
}
