// FhirCodeSystemController.java
package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.CodeSystemEntity;
import org.example.entity.ConceptEntity;
import org.example.service.FhirCodeSystemService;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fhir")
@Slf4j
public class FhirCodeSystemController {

    @Autowired
    private FhirCodeSystemService fhirService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Get all CodeSystems
    @GetMapping("/CodeSystem")
    public ResponseEntity<Map<String, Object>> getAllCodeSystems() {
        try {
            List<CodeSystemEntity> codeSystems = fhirService.getAllCodeSystems();

            return ResponseEntity.ok(Map.of(
                    "resourceType", "Bundle",
                    "type", "collection",
                    "total", codeSystems.size(),
                    "entry", codeSystems.stream().map(cs -> Map.of(
                            "resource", Map.of(
                                    "resourceType", "CodeSystem",
                                    "id", cs.getCodeSystemId(),
                                    "url", cs.getUrl(),
                                    "name", cs.getName(),
                                    "title", cs.getName(),
                                    "status", cs.getStatus(),
                                    "version", cs.getVersion(),
                                    "description", cs.getDescription(),
                                    "count", cs.getConcepts().size()
                            )
                    )).toArray()
            ));
        } catch (Exception e) {
            log.error("Error retrieving CodeSystems", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get specific CodeSystem by ID
    // Replace this entire section in your FhirCodeSystemController.java
    @GetMapping("/CodeSystem/{id}")
    public ResponseEntity<Object> getCodeSystemById(@PathVariable String id) {
        try {
            Optional<CodeSystemEntity> codeSystemOpt = fhirService.getCodeSystemById(id);

            if (codeSystemOpt.isPresent()) {
                CodeSystemEntity cs = codeSystemOpt.get();

                // Create concept list
                List<Map<String, Object>> concepts = cs.getConcepts().stream()
                        .map(concept -> {
                            Map<String, Object> conceptMap = new HashMap<>();
                            conceptMap.put("code", concept.getCode());
                            conceptMap.put("display", concept.getDisplay());
                            conceptMap.put("definition", concept.getDefinition() != null ? concept.getDefinition() : "");
                            return conceptMap;
                        }).collect(Collectors.toList());

                // Return the complete FHIR CodeSystem resource
                Map<String, Object> fhirResource = new HashMap<>();
                fhirResource.put("resourceType", "CodeSystem");
                fhirResource.put("id", cs.getCodeSystemId());
                fhirResource.put("url", cs.getUrl());
                fhirResource.put("name", cs.getName());
                fhirResource.put("title", cs.getName());
                fhirResource.put("status", cs.getStatus());
                fhirResource.put("version", cs.getVersion());
                fhirResource.put("description", cs.getDescription());
                fhirResource.put("count", cs.getConcepts().size());
                fhirResource.put("content", "complete");
                fhirResource.put("concept", concepts);

                return ResponseEntity.ok(fhirResource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error retrieving CodeSystem by ID: {}", id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // FHIR $lookup operation - lookup concept by code
    @GetMapping("/CodeSystem/$lookup")
    public ResponseEntity<Object> lookupConcept(
            @RequestParam(required = false) String system,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String version) {

        try {
            Optional<ConceptEntity> conceptOpt = fhirService.lookupConcept(system, code);

            if (conceptOpt.isPresent()) {
                ConceptEntity concept = conceptOpt.get();

                // Return FHIR Parameters resource for $lookup operation
                Map<String, Object> parameters = Map.of(
                        "resourceType", "Parameters",
                        "parameter", new Object[]{
                                Map.of("name", "name", "valueString", concept.getCodeSystem().getName()),
                                Map.of("name", "version", "valueString", concept.getCodeSystem().getVersion()),
                                Map.of("name", "display", "valueString", concept.getDisplay()),
                                Map.of("name", "definition", "valueString", concept.getDefinition() != null ? concept.getDefinition() : ""),
                                Map.of("name", "system", "valueUri", concept.getSystem())
                        }
                );

                return ResponseEntity.ok(parameters);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error in lookup operation - system: {}, code: {}", system, code, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Search concepts by various criteria
    @GetMapping("/CodeSystem/search")
    public ResponseEntity<Object> searchConcepts(
            @RequestParam(required = false) String namcCode,
            @RequestParam(required = false) String numcCode,
            @RequestParam(required = false) String arabicTerm,
            @RequestParam(required = false) String display,
            @RequestParam(required = false) String system,
            @RequestParam(defaultValue = "20") int limit) {

        try {
            List<ConceptEntity> concepts = fhirService.searchConcepts(namcCode, numcCode, arabicTerm, display, system, limit);

            Map<String, Object> bundle = Map.of(
                    "resourceType", "Bundle",
                    "type", "searchset",
                    "total", concepts.size(),
                    "entry", concepts.stream().map(concept -> Map.of(
                            "resource", Map.of(
                                    "resourceType", "Concept",
                                    "id", concept.getId(),
                                    "code", concept.getCode(),
                                    "display", concept.getDisplay(),
                                    "definition", concept.getDefinition() != null ? concept.getDefinition() : "",
                                    "system", concept.getSystem(),
                                    "codeSystemName", concept.getCodeSystem().getName()
                            )
                    )).toArray()
            );

            return ResponseEntity.ok(bundle);
        } catch (Exception e) {
            log.error("Error in concept search", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get concepts by category (Ayurveda, Unani, Siddha)
    @GetMapping("/CodeSystem/category/{category}")
    public ResponseEntity<Object> getConceptsByCategory(@PathVariable String category) {
        try {
            Optional<CodeSystemEntity> codeSystemOpt = fhirService.getCodeSystemByCategory(category);

            if (codeSystemOpt.isPresent()) {
                CodeSystemEntity cs = codeSystemOpt.get();

                Map<String, Object> response = Map.of(
                        "resourceType", "Bundle",
                        "type", "collection",
                        "total", cs.getConcepts().size(),
                        "category", category,
                        "codeSystemName", cs.getName(),
                        "codeSystemUrl", cs.getUrl(),
                        "concepts", cs.getConcepts().stream().map(concept -> Map.of(
                                "code", concept.getCode(),
                                "display", concept.getDisplay(),
                                "definition", concept.getDefinition() != null ? concept.getDefinition() : "",
                                "system", concept.getSystem()
                        )).toArray()
                );

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error retrieving concepts for category: {}", category, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Quick search endpoint for frontend
    @GetMapping("/search")
    public ResponseEntity<Object> quickSearch(@RequestParam String query, @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ConceptEntity> concepts = fhirService.quickSearch(query, limit);

            return ResponseEntity.ok(Map.of(
                    "total", concepts.size(),
                    "results", concepts.stream().map(concept -> Map.of(
                            "code", concept.getCode(),
                            "display", concept.getDisplay(),
                            "definition", concept.getDefinition() != null ? concept.getDefinition() : "",
                            "system", concept.getSystem(),
                            "category", concept.getCodeSystem().getName()
                    )).toArray()
            ));
        } catch (Exception e) {
            log.error("Error in quick search for query: {}", query, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}