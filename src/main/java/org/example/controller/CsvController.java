// CsvController.java
package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.entity.CodeSystemEntity;
import org.example.entity.ConceptEntity;
import org.example.service.CodeSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/codesystem")

public class CsvController {

    @Autowired
    private CodeSystemService codeSystemService;

    @PostMapping("/process-csv")
    public ResponseEntity<Map<String, Object>> processCsv(@RequestParam String filePath) {
        final Logger log = LoggerFactory.getLogger(CsvController.class);
        try {
            log.info("Processing CSV file: {}", filePath);
            Map<String, String> results = codeSystemService.processCSVToCodeSystems(filePath);

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "CSV processing completed",
                    "results", results
            ));

        } catch (Exception e) {
            log.error("Error processing CSV file: {}", filePath, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", "Failed to process CSV: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> getCodeSystemByCategory(@PathVariable String category) {
        final Logger log = LoggerFactory.getLogger(CsvController.class);
        try {
            Optional<CodeSystemEntity> codeSystem = codeSystemService.getCodeSystemByCategory(category);

            if (codeSystem.isPresent()) {
                CodeSystemEntity cs = codeSystem.get();
                return ResponseEntity.ok(Map.of(
                        "status", "SUCCESS",
                        "codeSystemId", cs.getCodeSystemId(),
                        "name", cs.getName(),
                        "url", cs.getUrl(),
                        "version", cs.getVersion(),
                        "conceptCount", cs.getConcepts().size(),
                        "description", cs.getDescription()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error retrieving CodeSystem for category: {}", category, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", "Failed to retrieve CodeSystem: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/lookup")
    public ResponseEntity<Map<String, Object>> lookupConcept(

            @RequestParam String system,
            @RequestParam String code) {
        final Logger log = LoggerFactory.getLogger(CsvController.class);

        try {
            Optional<ConceptEntity> concept = codeSystemService.lookupConcept(system, code);

            if (concept.isPresent()) {
                ConceptEntity c = concept.get();
                return ResponseEntity.ok(Map.of(
                        "status", "SUCCESS",
                        "code", c.getCode(),
                        "display", c.getDisplay(),
                        "definition", c.getDefinition() != null ? c.getDefinition() : "",
                        "system", c.getSystem()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error looking up concept - system: {}, code: {}", system, code, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", "Failed to lookup concept: " + e.getMessage()
            ));
        }
    }

    // Test endpoint to process a sample CSV (you can modify the path)
    @PostMapping("/test-process")
    public ResponseEntity<Map<String, Object>> testProcessCsv() {
        // Change this path to your actual CSV file location
        String testFilePath = "src/main/resources/sample-data.csv";
        return processCsv(testFilePath);
    }
}