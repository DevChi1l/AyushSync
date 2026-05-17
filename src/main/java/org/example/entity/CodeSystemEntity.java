package org.example.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "code_systems")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeSystemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_system_id", unique = true, nullable = false)
    private String codeSystemId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String version;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String status; // active, inactive, etc.

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Lob
    @Column(name = "fhir_resource", columnDefinition = "TEXT")
    private String fhirResourceJson;

    @OneToMany(mappedBy = "codeSystem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ConceptEntity> concepts = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getCodeSystemId() { return codeSystemId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getVersion() { return version; }
    public String getUrl() { return url; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getFhirResourceJson() { return fhirResourceJson; }
    public List<ConceptEntity> getConcepts() { return concepts; }


}
