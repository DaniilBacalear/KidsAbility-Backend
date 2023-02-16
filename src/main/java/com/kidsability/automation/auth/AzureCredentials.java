package com.kidsability.automation.auth;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Component
@Getter
public class AzureCredentials {
    @Value("${azure_credentials.grant_type}")
    private String grantType;
    @Value("${azure_credentials.client_id}")
    private String clientId;
    @Value("${azure_credentials.client_secret}")
    private String clientSecret;
    @Value("${azure_credentials.resource}")
    private String resource;
    @Value("${azure_credentials.tenant_id}")
    private String tenantId;
}
