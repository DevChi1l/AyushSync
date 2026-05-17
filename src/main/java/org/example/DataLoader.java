//package org.example;
//
//import org.example.service.CodeSystemService;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//public class DataLoader implements CommandLineRunner {
//
//    private final CodeSystemService codeSystemService;
//
//    public DataLoader(CodeSystemService codeSystemService) {
//        this.codeSystemService = codeSystemService;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        String csvPath = "/home/meowski/ayushdata/merged_ayush_codes.csv"; // adjust path to your CSV
//        codeSystemService.processCSVToCodeSystems(csvPath);
//    }
//}
