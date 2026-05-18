// ============================================================
// HogarRepository.java
// ============================================================
package com.appcasa.domain.hogar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HogarRepository extends JpaRepository<Hogar, UUID> {

  Optional<Hogar> findByCodigo(String codigo);

  boolean existsByCodigo(String codigo);
}
