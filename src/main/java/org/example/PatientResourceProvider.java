package org.example;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PatientResourceProvider implements IResourceProvider {

    // In-memory storage for demo purposes
    private Map<String, Patient> patients = new HashMap<>();

    public PatientResourceProvider() {
        // Create some sample patients
        createSamplePatients();
    }

    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }

    /**
     * Read operation - get patient by ID
     * URL: GET /fhir/Patient/{id}
     */
    @Read
    public Patient getPatientById(@IdParam IdType theId) {
        String patientId = theId.getIdPart();
        Patient patient = patients.get(patientId);

        if (patient == null) {
            throw new ResourceNotFoundException("Patient with ID " + patientId + " not found");
        }

        return patient;
    }

    /**
     * Search operation - get all patients
     * URL: GET /fhir/Patient
     */
    @Search
    public List<Patient> getAllPatients() {
        return new ArrayList<>(patients.values());
    }

    private void createSamplePatients() {
        // Patient 1
        Patient patient1 = new Patient();
        patient1.setId("1");
        patient1.addName()
                .setFamily("Doe")
                .addGiven("John")
                .addGiven("William");
        patient1.addTelecom()
                .setSystem(org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem.PHONE)
                .setValue("+1-555-123-4567");
        patient1.setGender(org.hl7.fhir.r4.model.Enumerations.AdministrativeGender.MALE);
        patient1.setBirthDateElement(new org.hl7.fhir.r4.model.DateType("1985-05-15"));

        patients.put("1", patient1);

        // Patient 2
        Patient patient2 = new Patient();
        patient2.setId("2");
        patient2.addName()
                .setFamily("Smith")
                .addGiven("Jane")
                .addGiven("Marie");
        patient2.addTelecom()
                .setSystem(org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem.EMAIL)
                .setValue("jane.smith@example.com");
        patient2.setGender(org.hl7.fhir.r4.model.Enumerations.AdministrativeGender.FEMALE);
        patient2.setBirthDateElement(new org.hl7.fhir.r4.model.DateType("1992-08-22"));

        patients.put("2", patient2);

        // Patient 3
        Patient patient3 = new Patient();
        patient3.setId("3");
        patient3.addName()
                .setFamily("Kumar")
                .addGiven("Raj")
                .addGiven("Singh");
        patient3.addTelecom()
                .setSystem(org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem.PHONE)
                .setValue("+91-98765-43210");
        patient3.setGender(org.hl7.fhir.r4.model.Enumerations.AdministrativeGender.MALE);
        patient3.setBirthDateElement(new org.hl7.fhir.r4.model.DateType("1978-12-10"));

        patients.put("3", patient3);
    }
}