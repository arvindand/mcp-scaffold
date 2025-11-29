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
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.petclinic.model.Owner;

/**
 * Repository for managing pet owners.
 *
 * <p>Provides methods for finding owners by various criteria including name, city, and telephone
 * number.
 *
 * @author Arvind Menon
 */
@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {

  /**
   * Find owners by last name (case-insensitive, partial match).
   *
   * @param lastName the last name to search for
   * @return list of matching owners
   */
  List<Owner> findByLastNameContainingIgnoreCase(String lastName);

  /**
   * Find an owner by their telephone number.
   *
   * @param telephone the telephone number
   * @return the owner if found
   */
  Optional<Owner> findByTelephone(String telephone);

  /**
   * Find all owners in a specific city.
   *
   * @param city the city name
   * @return list of owners in that city
   */
  List<Owner> findByCity(String city);

  /**
   * Find owners with a specific first and last name.
   *
   * @param firstName the first name
   * @param lastName the last name
   * @return list of matching owners
   */
  List<Owner> findByFirstNameAndLastName(String firstName, String lastName);

  /**
   * Count owners in a specific city.
   *
   * @param city the city name
   * @return the count of owners
   */
  long countByCity(String city);

  /**
   * Check if an owner exists with the given telephone.
   *
   * @param telephone the telephone number
   * @return true if exists
   */
  boolean existsByTelephone(String telephone);

  /**
   * Find owners who have at least one pet.
   *
   * @return list of owners with pets
   */
  @Query("SELECT DISTINCT o FROM Owner o JOIN o.pets p")
  List<Owner> findOwnersWithPets();
}
