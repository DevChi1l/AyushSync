// CodeSystemResourceProvider.java
package org.example;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.example.entity.CodeSystemEntity;
import org.example.entity.ConceptEntity;
import org.example.service.FhirCodeSystemService;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
@Component
@Transactional(readOnly = true)
public class CodeSystemResourceProvider implements IResourceProvider {

    @Autowired
    private FhirCodeSystemService fhirCodeSystemService;

    @Override
    public Class<CodeSystem> getResourceType() {
        return CodeSystem.class;
    }

    /**
     * Read operation - get CodeSystem by ID
     * URL: GET /fhir/CodeSystem/{id}
     */
    @Read
    public CodeSystem getCodeSystemById(@IdParam IdType theId) {
        String codeSystemId = theId.getIdPart();
        Optional<CodeSystemEntity> codeSystemOpt = fhirCodeSystemService.getCodeSystemById(codeSystemId);

        if (codeSystemOpt.isEmpty()) {
            throw new ResourceNotFoundException("CodeSystem with ID " + codeSystemId + " not found");
        }

        // Force initialization of concepts collection
        CodeSystemEntity entity = codeSystemOpt.get();
        entity.getConcepts().size(); // This will trigger lazy loading

        return convertToFhirCodeSystem(entity);
    }

    /**
     * Search operation - get all CodeSystems or search by parameters
     * URL: GET /fhir/CodeSystem?url={url}&name={name}
     */
    @Search
    public List<CodeSystem> searchCodeSystems(
            @OptionalParam(name = CodeSystem.SP_URL) StringParam url,
            @OptionalParam(name = CodeSystem.SP_NAME) StringParam name,
            @OptionalParam(name = CodeSystem.SP_STATUS) TokenParam status) {

        List<CodeSystemEntity> codeSystemEntities = fhirCodeSystemService.getAllCodeSystems();
        List<CodeSystem> fhirCodeSystems = new ArrayList<>();

        for (CodeSystemEntity entity : codeSystemEntities) {
            // Apply filtering if parameters are provided
            boolean matches = true;

            if (url != null && !entity.getUrl().contains(url.getValue())) {
                matches = false;
            }

            if (name != null && !entity.getName().toLowerCase().contains(name.getValue().toLowerCase())) {
                matches = false;
            }

            if (status != null && !entity.getStatus().equals(status.getValue())) {
                matches = false;
            }

            if (matches) {
                fhirCodeSystems.add(convertToFhirCodeSystem(entity));
            }
        }

        return fhirCodeSystems;
    }

    /**
     * $lookup operation for CodeSystem
     * URL: GET /fhir/CodeSystem/$lookup?system={system}&code={code}
     */
    @Operation(name = "$lookup", idempotent = true)
    public Parameters lookup(
            @OperationParam(name = "system") UriType system,
            @OperationParam(name = "code") CodeType code,
            @OperationParam(name = "version") StringType version) {

        String systemUrl = system != null ? system.getValue() : null;
        String codeValue = code != null ? code.getValue() : null;

        Optional<ConceptEntity> conceptOpt = fhirCodeSystemService.lookupConcept(systemUrl, codeValue);

        if (conceptOpt.isEmpty()) {
            throw new ResourceNotFoundException("Concept not found for system: " + systemUrl + ", code: " + codeValue);
        }

        ConceptEntity concept = conceptOpt.get();

        Parameters parameters = new Parameters();
        parameters.addParameter("name", new StringType(concept.getCodeSystem().getName()));
        parameters.addParameter("version", new StringType(concept.getCodeSystem().getVersion()));
        parameters.addParameter("display", new StringType(concept.getDisplay()));
        parameters.addParameter("definition", new StringType(concept.getDefinition() != null ? concept.getDefinition() : ""));
        parameters.addParameter("system", new UriType(concept.getSystem()));

        return parameters;
    }

    /**
     * $validate-code operation for CodeSystem
     * URL: GET /fhir/CodeSystem/$validate-code?url={url}&code={code}
     */
    @Operation(name = "$validate-code", idempotent = true)
    public Parameters validateCode(
            @OperationParam(name = "url") UriType url,
            @OperationParam(name = "code") CodeType code,
            @OperationParam(name = "display") StringType display) {

        String systemUrl = url != null ? url.getValue() : null;
        String codeValue = code != null ? code.getValue() : null;

        Optional<ConceptEntity> conceptOpt = fhirCodeSystemService.lookupConcept(systemUrl, codeValue);

        Parameters parameters = new Parameters();

        if (conceptOpt.isPresent()) {
            ConceptEntity concept = conceptOpt.get();
            parameters.addParameter("result", new BooleanType(true));
            parameters.addParameter("display", new StringType(concept.getDisplay()));
        } else {
            parameters.addParameter("result", new BooleanType(false));
            parameters.addParameter("message", new StringType("Code not found in system"));
        }

        return parameters;
    }

    /**
     * Convert CodeSystemEntity to FHIR CodeSystem resource
     */
    private CodeSystem convertToFhirCodeSystem(CodeSystemEntity entity) {
        CodeSystem codeSystem = new CodeSystem();

        codeSystem.setId(entity.getCodeSystemId());
        codeSystem.setUrl(entity.getUrl());
        codeSystem.setName(entity.getName());
        codeSystem.setTitle(entity.getName());
        codeSystem.setDescription(entity.getDescription());
        codeSystem.setVersion(entity.getVersion());
        codeSystem.setStatus(Enumerations.PublicationStatus.fromCode(entity.getStatus().toLowerCase()));
        codeSystem.setContent(CodeSystem.CodeSystemContentMode.COMPLETE);
        codeSystem.setCount(entity.getConcepts().size());

        // Add concepts
        for (ConceptEntity conceptEntity : entity.getConcepts()) {
            CodeSystem.ConceptDefinitionComponent concept = new CodeSystem.ConceptDefinitionComponent();
            concept.setCode(conceptEntity.getCode());
            concept.setDisplay(conceptEntity.getDisplay());
            if (conceptEntity.getDefinition() != null) {
                concept.setDefinition(conceptEntity.getDefinition());
            }
            codeSystem.addConcept(concept);
        }

        return codeSystem;
    }
}