// ConceptRepository.java
package org.example.repository;

import org.example.entity.ConceptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConceptRepository extends JpaRepository<ConceptEntity, Long> {

    // Find concept by system URL and code
    @Query("SELECT c FROM ConceptEntity c WHERE c.system = :systemUrl AND c.code = :code")
    Optional<ConceptEntity> findBySystemUrlAndCode(@Param("systemUrl") String systemUrl, @Param("code") String code);

    // Find concepts by code system ID
    @Query("SELECT c FROM ConceptEntity c WHERE c.codeSystem.codeSystemId = :codeSystemId")
    List<ConceptEntity> findByCodeSystemId(@Param("codeSystemId") String codeSystemId);

    // Find concepts by code (across all systems)
    List<ConceptEntity> findByCode(String code);

    // Find concepts by display name (partial match)
    @Query("SELECT c FROM ConceptEntity c WHERE LOWER(c.display) LIKE LOWER(CONCAT('%', :display, '%'))")
    List<ConceptEntity> findByDisplayContaining(@Param("display") String display);

    // Find concepts by system URL
    List<ConceptEntity> findBySystem(String system);

    // Search concepts by multiple criteria
    @Query("SELECT c FROM ConceptEntity c WHERE " +
            "(:code IS NULL OR LOWER(c.code) LIKE LOWER(CONCAT('%', :code, '%'))) AND " +
            "(:display IS NULL OR LOWER(c.display) LIKE LOWER(CONCAT('%', :display, '%'))) AND " +
            "(:system IS NULL OR LOWER(c.system) LIKE LOWER(CONCAT('%', :system, '%')))")
    List<ConceptEntity> findByCriteria(@Param("code") String code,
                                       @Param("display") String display,
                                       @Param("system") String system);
}