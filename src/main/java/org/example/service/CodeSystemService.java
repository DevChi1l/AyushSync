// CodeSystemService.java
package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.CodeSystemEntity;
import org.example.entity.ConceptEntity;
import org.example.model.CsvRecord;
import org.example.repository.CodeSystemRepository;
import org.example.repository.ConceptRepository;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Enumerations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CodeSystemService {

    @Autowired
    private CodeSystemRepository codeSystemRepository;

    @Autowired
    private ConceptRepository conceptRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public Map<String, String> processCSVToCodeSystems(String csvFilePath) throws IOException {
        log.info("Starting CSV processing from file: {}", csvFilePath);

        // Read CSV file
        List<CsvRecord> csvRecords = readCsvFile(csvFilePath);
        log.info("Read {} records from CSV", csvRecords.size());

        // Group records by category
        Map<String, List<CsvRecord>> recordsByCategory = csvRecords.stream()
                .filter(record -> record.getCategory() != null && !record.getCategory().trim().isEmpty())
                .collect(Collectors.groupingBy(record -> record.getCategory().trim()));

        Map<String, String> results = new HashMap<>();

        // Process each category
        for (Map.Entry<String, List<CsvRecord>> entry : recordsByCategory.entrySet()) {
            String category = entry.getKey();
            List<CsvRecord> records = entry.getValue();

            try {
                String codeSystemId = createCodeSystemForCategory(category, records);
                results.put(category, "SUCCESS - CodeSystem ID: " + codeSystemId);
                log.info("Successfully created CodeSystem for category: {} with {} concepts",
                        category, records.size());
            } catch (Exception e) {
                results.put(category, "FAILED: " + e.getMessage());
                log.error("Failed to create CodeSystem for category: {}", category, e);
            }
        }

        return results;
    }

    private List<CsvRecord> readCsvFile(String csvFilePath) throws IOException {
        try (FileReader reader = new FileReader(csvFilePath)) {
            return new CsvToBeanBuilder<CsvRecord>(reader)
                    .withType(CsvRecord.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build()
                    .parse();
        }
    }

    private String createCodeSystemForCategory(String category, List<CsvRecord> records) throws Exception {
        // Create CodeSystem entity
        String codeSystemId = generateCodeSystemId(category);
        String url = generateCodeSystemUrl(category);

        // Check if CodeSystem already exists
        Optional<CodeSystemEntity> existingCodeSystem = codeSystemRepository.findByCodeSystemId(codeSystemId);
        if (existingCodeSystem.isPresent()) {
            log.info("CodeSystem already exists for category: {}, updating...", category);
            return updateExistingCodeSystem(existingCodeSystem.get(), records);
        }

        // Create new CodeSystem
        CodeSystemEntity codeSystemEntity = new CodeSystemEntity();
        codeSystemEntity.setCodeSystemId(codeSystemId);
        codeSystemEntity.setName(generateCodeSystemName(category));
        codeSystemEntity.setDescription(generateCodeSystemDescription(category));
        codeSystemEntity.setVersion("1.0.0");
        codeSystemEntity.setUrl(url);
        codeSystemEntity.setStatus("active");

        // Create FHIR CodeSystem resource
        CodeSystem fhirCodeSystem = createFhirCodeSystem(category, records, url);


        // Save CodeSystem
        CodeSystemEntity savedCodeSystem = codeSystemRepository.save(codeSystemEntity);

        // Create and save concepts
        List<ConceptEntity> concepts = createConcepts(records, savedCodeSystem, url);
        conceptRepository.saveAll(concepts);

        log.info("Created CodeSystem '{}' with {} concepts", codeSystemId, concepts.size());
        return codeSystemId;
    }

    private String updateExistingCodeSystem(CodeSystemEntity existingCodeSystem, List<CsvRecord> records) throws Exception {
        // Delete existing concepts
        conceptRepository.deleteAll(existingCodeSystem.getConcepts());

        // Create new concepts
        List<ConceptEntity> newConcepts = createConcepts(records, existingCodeSystem, existingCodeSystem.getUrl());
        conceptRepository.saveAll(newConcepts);

        // Update FHIR resource
        CodeSystem fhirCodeSystem = createFhirCodeSystem(
                existingCodeSystem.getName().replace(" CodeSystem", ""),
                records,
                existingCodeSystem.getUrl()
        );
        existingCodeSystem.setFhirResourceJson(objectMapper.writeValueAsString(fhirCodeSystem));
        codeSystemRepository.save(existingCodeSystem);

        log.info("Updated existing CodeSystem '{}' with {} concepts",
                existingCodeSystem.getCodeSystemId(), newConcepts.size());
        return existingCodeSystem.getCodeSystemId();
    }

    private List<ConceptEntity> createConcepts(List<CsvRecord> records, CodeSystemEntity codeSystem, String systemUrl) {
        List<ConceptEntity> concepts = new ArrayList<>();

        for (CsvRecord record : records) {
            ConceptEntity concept = new ConceptEntity();
            concept.setCode(record.getCodeForCategory());
            concept.setDisplay(record.getTermForCategory());
            concept.setDefinition(record.getShortDefinition() != null ?
                    record.getShortDefinition() : record.getLongDefinition());
            concept.setSystem(systemUrl);
            concept.setActive(true);
            concept.setCodeSystem(codeSystem);

            concepts.add(concept);
        }

        return concepts;
    }
    
    private CodeSystem createFhirCodeSystem(String category, List<CsvRecord> records, String url) {
        CodeSystem codeSystem = new CodeSystem();
        codeSystem.setId(generateCodeSystemId(category));
        codeSystem.setUrl(url);
        codeSystem.setName(generateCodeSystemName(category));
        codeSystem.setTitle(generateCodeSystemTitle(category));
        codeSystem.setDescription(generateCodeSystemDescription(category));
        codeSystem.setStatus(Enumerations.PublicationStatus.ACTIVE);
        codeSystem.setContent(CodeSystem.CodeSystemContentMode.COMPLETE);
        codeSystem.setCount(records.size());
        codeSystem.setVersion("1.0.0");

        // Add concepts to FHIR CodeSystem
        for (CsvRecord record : records) {
            CodeSystem.ConceptDefinitionComponent concept = new CodeSystem.ConceptDefinitionComponent();
            concept.setCode(record.getCodeForCategory());
            concept.setDisplay(record.getTermForCategory());

            if (record.getShortDefinition() != null || record.getLongDefinition() != null) {
                concept.setDefinition(record.getShortDefinition() != null ?
                        record.getShortDefinition() : record.getLongDefinition());
            }

            codeSystem.addConcept(concept);
        }

        return codeSystem;
    }

    private String generateCodeSystemId(String category) {
        return category.toLowerCase() + "-medicine-codes";
    }

    private String generateCodeSystemUrl(String category) {
        return "http://example.org/fhir/CodeSystem/" + generateCodeSystemId(category);
    }

    private String generateCodeSystemName(String category) {
        return category + " Medicine Codes";
    }

    private String generateCodeSystemTitle(String category) {
        return category + " Traditional Medicine CodeSystem";
    }

    private String generateCodeSystemDescription(String category) {
        return "CodeSystem containing " + category + " traditional medicine codes and terms";
    }

    // Method to get a CodeSystem by category for FHIR operations
    public Optional<CodeSystemEntity> getCodeSystemByCategory(String category) {
        String codeSystemId = generateCodeSystemId(category);
        return codeSystemRepository.findByCodeSystemId(codeSystemId);
    }

    // Method to look up a concept
    public Optional<ConceptEntity> lookupConcept(String system, String code) {
        return conceptRepository.findBySystemUrlAndCode(system, code);
    }
}