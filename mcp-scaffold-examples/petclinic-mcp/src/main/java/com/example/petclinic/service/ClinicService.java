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
package com.example.petclinic.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petclinic.model.Owner;
import com.example.petclinic.model.Pet;
import com.example.petclinic.model.PetType;
import com.example.petclinic.repository.OwnerRepository;
import com.example.petclinic.repository.PetRepository;

/**
 * Business service for clinic operations.
 *
 * <p>Provides higher-level operations that combine repository access with business logic.
 *
 * @author Arvind Menon
 */
@Service
@Transactional
public class ClinicService {

  private final OwnerRepository ownerRepository;
  private final PetRepository petRepository;

  public ClinicService(OwnerRepository ownerRepository, PetRepository petRepository) {
    this.ownerRepository = ownerRepository;
    this.petRepository = petRepository;
  }

  /**
   * Register a new owner with their pet.
   *
   * @param firstName owner's first name
   * @param lastName owner's last name
   * @param petName pet's name
   * @param petType pet's type
   * @return the created owner with their pet
   */
  public Owner registerOwnerWithPet(
      String firstName, String lastName, String petName, PetType petType) {
    var owner = new Owner(firstName, lastName);
    var pet = new Pet(petName, petType);
    owner.addPet(pet);
    return ownerRepository.save(owner);
  }

  /**
   * Generate statistics about pets in the clinic.
   *
   * @return map of pet type to count
   */
  @Transactional(readOnly = true)
  public Map<PetType, Long> generatePetStatistics() {
    return petRepository.findAll().stream()
        .collect(Collectors.groupingBy(Pet::getType, Collectors.counting()));
  }

  /**
   * Find owners who have pets of a specific type.
   *
   * @param petType the pet type to search for
   * @return list of owners with pets of that type
   */
  @Transactional(readOnly = true)
  public List<Owner> findOwnersByPetType(PetType petType) {
    return petRepository.findByType(petType).stream().map(Pet::getOwner).distinct().toList();
  }

  /**
   * Get the total count of pets and owners.
   *
   * @return a summary string
   */
  @Transactional(readOnly = true)
  public String getClinicSummary() {
    long ownerCount = ownerRepository.count();
    long petCount = petRepository.count();
    return "Clinic has " + ownerCount + " owners and " + petCount + " pets";
  }

  /**
   * Transfer a pet to a new owner.
   *
   * @param petId the pet's ID
   * @param newOwnerId the new owner's ID
   * @return the updated pet
   */
  public Pet transferPet(Long petId, Long newOwnerId) {
    var pet =
        petRepository
            .findById(petId)
            .orElseThrow(() -> new IllegalArgumentException("Pet not found"));
    var newOwner =
        ownerRepository
            .findById(newOwnerId)
            .orElseThrow(() -> new IllegalArgumentException("Owner not found"));

    pet.setOwner(newOwner);
    return petRepository.save(pet);
  }
}
