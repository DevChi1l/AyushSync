// FhirCodeSystemService.java
package org.example.service;

import org.example.entity.CodeSystemEntity;
import org.example.entity.ConceptEntity;
import org.example.repository.CodeSystemRepository;
import org.example.repository.ConceptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true) // Add this for lazy loading support
public class FhirCodeSystemService {

    private static final Logger log = LoggerFactory.getLogger(FhirCodeSystemService.class);

    @Autowired
    private CodeSystemRepository codeSystemRepository;

    @Autowired
    private ConceptRepository conceptRepository;

    public List<CodeSystemEntity> getAllCodeSystems() {
        List<CodeSystemEntity> codeSystems = codeSystemRepository.findAll();
        // Eagerly load concepts for each code system
        codeSystems.forEach(cs -> cs.getConcepts().size());
        return codeSystems;
    }

    public Optional<CodeSystemEntity> getCodeSystemById(String id) {
        Optional<CodeSystemEntity> codeSystem = codeSystemRepository.findByCodeSystemId(id);
        // Eagerly load concepts if code system exists
        if (codeSystem.isPresent()) {
            codeSystem.get().getConcepts().size(); // Force lazy loading
        }
        return codeSystem;
    }

    public Optional<CodeSystemEntity> getCodeSystemByCategory(String category) {
        String codeSystemId = generateCodeSystemId(category);
        Optional<CodeSystemEntity> codeSystem = codeSystemRepository.findByCodeSystemId(codeSystemId);
        // Eagerly load concepts if code system exists
        if (codeSystem.isPresent()) {
            codeSystem.get().getConcepts().size(); // Force lazy loading
        }
        return codeSystem;
    }

    public Optional<ConceptEntity> lookupConcept(String system, String code) {
        if (system != null && code != null) {
            return conceptRepository.findBySystemUrlAndCode(system, code);
        } else if (code != null) {
            // Search by code across all systems
            List<ConceptEntity> concepts = conceptRepository.findAll();
            return concepts.stream()
                    .filter(c -> c.getCode().equals(code))
                    .findFirst();
        }
        return Optional.empty();
    }

    public List<ConceptEntity> searchConcepts(String namcCode, String numcCode, String arabicTerm,
                                              String display, String system, int limit) {

        List<ConceptEntity> allConcepts = conceptRepository.findAll();
        List<ConceptEntity> filteredConcepts = new ArrayList<>();

        for (ConceptEntity concept : allConcepts) {
            boolean matches = true;

            // Filter by NAMC_CODE
            if (namcCode != null && !namcCode.isEmpty()) {
                matches &= concept.getCode().toLowerCase().contains(namcCode.toLowerCase());
            }

            // Filter by NUMC_CODE
            if (numcCode != null && !numcCode.isEmpty()) {
                matches &= concept.getCode().toLowerCase().contains(numcCode.toLowerCase());
            }

            // Filter by Arabic term (this would be in the display field for Unani)
            if (arabicTerm != null && !arabicTerm.isEmpty()) {
                matches &= (concept.getDisplay() != null &&
                        concept.getDisplay().toLowerCase().contains(arabicTerm.toLowerCase())) ||
                        (concept.getDefinition() != null &&
                                concept.getDefinition().toLowerCase().contains(arabicTerm.toLowerCase()));
            }

            // Filter by display
            if (display != null && !display.isEmpty()) {
                matches &= concept.getDisplay() != null &&
                        concept.getDisplay().toLowerCase().contains(display.toLowerCase());
            }

            // Filter by system
            if (system != null && !system.isEmpty()) {
                matches &= concept.getSystem() != null &&
                        concept.getSystem().toLowerCase().contains(system.toLowerCase());
            }

            if (matches) {
                filteredConcepts.add(concept);
                if (filteredConcepts.size() >= limit) {
                    break;
                }
            }
        }

        return filteredConcepts;
    }

    public List<ConceptEntity> quickSearch(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<ConceptEntity> allConcepts = conceptRepository.findAll();
        List<ConceptEntity> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (ConceptEntity concept : allConcepts) {
            boolean matches = false;

            // Search in code
            if (concept.getCode() != null && concept.getCode().toLowerCase().contains(lowerQuery)) {
                matches = true;
            }

            // Search in display
            if (concept.getDisplay() != null && concept.getDisplay().toLowerCase().contains(lowerQuery)) {
                matches = true;
            }

            // Search in definition
            if (concept.getDefinition() != null && concept.getDefinition().toLowerCase().contains(lowerQuery)) {
                matches = true;
            }

            if (matches) {
                results.add(concept);
                if (results.size() >= limit) {
                    break;
                }
            }
        }

        return results;
    }

    // Get concepts by specific code type
    public List<ConceptEntity> getConceptsByCodeType(String codeType, String codeValue) {
        List<ConceptEntity> allConcepts = conceptRepository.findAll();
        List<ConceptEntity> results = new ArrayList<>();

        for (ConceptEntity concept : allConcepts) {
            boolean matches = false;

            switch (codeType.toLowerCase()) {
                case "namc":
                case "namc_code":
                    // For Ayurveda and Siddha systems
                    if (concept.getCodeSystem().getName().toLowerCase().contains("ayurveda") ||
                            concept.getCodeSystem().getName().toLowerCase().contains("siddha")) {
                        matches = concept.getCode().equalsIgnoreCase(codeValue);
                    }
                    break;

                case "numc":
                case "numc_code":
                    // For Unani system
                    if (concept.getCodeSystem().getName().toLowerCase().contains("unani")) {
                        matches = concept.getCode().equalsIgnoreCase(codeValue);
                    }
                    break;

                default:
                    matches = concept.getCode().equalsIgnoreCase(codeValue);
                    break;
            }

            if (matches) {
                results.add(concept);
            }
        }

        return results;
    }

    private String generateCodeSystemId(String category) {
        return category.toLowerCase() + "-medicine-codes";
    }
}