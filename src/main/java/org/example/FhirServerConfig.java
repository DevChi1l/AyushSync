package org.example;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
public class FhirServerConfig {

    @Autowired
    private PatientResourceProvider patientResourceProvider;

    @Autowired
    private CodeSystemResourceProvider codeSystemResourceProvider;

    @Bean
    public ServletRegistrationBean<RestfulServer> fhirServletRegistration() {
        ServletRegistrationBean<RestfulServer> registration =
                new ServletRegistrationBean<>(new FhirRestfulServer(), "/fhir/*");
        registration.setLoadOnStartup(1);
        return registration;
    }

    public class FhirRestfulServer extends RestfulServer {
        @Override
        protected void initialize() {
            // Set FHIR version to R4
            setFhirContext(FhirContext.forR4());

            // Register resource providers - now including both Patient and CodeSystem
            setResourceProviders(patientResourceProvider, codeSystemResourceProvider);

            // Enable CORS
            CorsConfiguration config = new CorsConfiguration();
            config.addAllowedHeader("*");
            config.addAllowedOrigin("*");
            config.addAllowedMethod("*");

            CorsInterceptor corsInterceptor = new CorsInterceptor(config);
            registerInterceptor(corsInterceptor);

            // Set server name and description
            setServerName("SIH FHIR Server - Traditional Medicine CodeSystems");
            setServerVersion("1.0.0");
        }
    }
}