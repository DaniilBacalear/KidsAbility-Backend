package com.kidsability.automation.util;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidsability.automation.auth.AzureCredentials;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;


public class GraphApiUtil {
    public static String getToken(AzureCredentials azureCredentials) throws IOException {
        var restTemplate = new RestTemplate();
        var httpHeaders = new HttpHeaders();
        var uri = "https://login.microsoft.com/" + azureCredentials.getTenantId() + "/oauth2/token";
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        var body = new LinkedMultiValueMap<String, String>();
        body.add("client_id", azureCredentials.getClientId());
        body.add("client_secret", azureCredentials.getClientSecret());
        body.add("resource", azureCredentials.getResource());
        body.add("grant_type", azureCredentials.getGrantType());

        var request = new HttpEntity<>(body, httpHeaders);
        var resJsonStr = restTemplate.postForObject(uri, request, String.class);

        var map = new ObjectMapper().readValue(resJsonStr, HashMap.class);
        return (String) map.get("access_token");
    }

    public static GraphServiceClient getGraphClient(AzureCredentials azureCredentials) {
        final ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(azureCredentials.getClientId())
                .clientSecret(azureCredentials.getClientSecret())
                .tenantId(azureCredentials.getTenantId())
                .build();
        var scopes = List.of(
//                "https://graph.microsoft.com/User.Read",
//                "https://graph.microsoft.com/Files.ReadWrite.All"
                "https://graph.microsoft.com/.default"
        );

        final TokenCredentialAuthProvider tokenCredentialAuthProvider = new TokenCredentialAuthProvider(scopes, clientSecretCredential);

        final GraphServiceClient graphClient =
                GraphServiceClient
                        .builder()
                        .authenticationProvider(tokenCredentialAuthProvider)
                        .buildClient();

        return graphClient;

//        final User me = graphClient.me().buildRequest().get();
//        me.
    }
}
