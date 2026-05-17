// CodeSystemRepository.java
package org.example.repository;

import org.example.entity.CodeSystemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodeSystemRepository extends JpaRepository<CodeSystemEntity, Long> {

    Optional<CodeSystemEntity> findByCodeSystemId(String codeSystemId);

    Optional<CodeSystemEntity> findByUrl(String url);

    Optional<CodeSystemEntity> findByName(String name);

    @Query("SELECT cs FROM CodeSystemEntity cs WHERE cs.url = :url AND cs.version = :version")
    Optional<CodeSystemEntity> findByUrlAndVersion(@Param("url") String url, @Param("version") String version);

    boolean existsByCodeSystemId(String codeSystemId);
}

