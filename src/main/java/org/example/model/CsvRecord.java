// CsvRecord.java
package org.example.model;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CsvRecord {

    @CsvBindByName(column = "FHIR_ID")
    private String fhirId;

    @CsvBindByName(column = "Arabic_term")
    private String arabicTerm;

    @CsvBindByName(column = "Long_definition")
    private String longDefinition;

    @CsvBindByName(column = "NAMC_CODE")
    private String namcCode;

    @CsvBindByName(column = "NAMC_ID")
    private String namcId;

    @CsvBindByName(column = "NAMC_TERM")
    private String namcTerm;

    @CsvBindByName(column = "NAMC_term")
    private String namcTermAlt;

    @CsvBindByName(column = "NAMC_term_DEVANAGARI")
    private String namcTermDevanagari;

    @CsvBindByName(column = "NAMC_term_diacritical")
    private String namcTermDiacritical;

    @CsvBindByName(column = "NUMC_CODE")
    private String numcCode;

    @CsvBindByName(column = "NUMC_ID")
    private String numcId;

    @CsvBindByName(column = "NUMC_TERM")
    private String numcTerm;

    @CsvBindByName(column = "Ontology_branches")
    private String ontologyBranches;

    @CsvBindByName(column = "Reference")
    private String reference;

    @CsvBindByName(column = "Short_definition")
    private String shortDefinition;

    @CsvBindByName(column = "Tamil_term")
    private String tamilTerm;

    @CsvBindByName(column = "Category")
    private String category;

    // Helper method to get the appropriate code based on category
    public String getCodeForCategory() {
        if (category == null || category.trim().isEmpty()) return null;

        String cat = category.trim().toLowerCase();
        switch (cat) {
            case "ayurveda":
            case "siddha":
                return namcCode;
            case "unani":
                return numcCode;
            default:
                return null;
        }
    }

    // Helper method to get the appropriate term based on category
    public String getTermForCategory() {
        if (category == null || category.trim().isEmpty()) return null;

        String cat = category.trim().toLowerCase();
        switch (cat) {
            case "ayurveda":
            case "siddha":
                return namcTerm != null ? namcTerm : namcTermAlt;
            case "unani":
                return numcTerm;
            default:
                return null;
        }
    }

    // Check if this record has valid data for processing
    public boolean isValidForCategory() {
        if (category == null || category.trim().isEmpty()) return false;

        String code = getCodeForCategory();
        String term = getTermForCategory();
        return code != null && !code.trim().isEmpty() &&
                term != null && !term.trim().isEmpty() &&
                fhirId != null && !fhirId.trim().isEmpty();
    }
}