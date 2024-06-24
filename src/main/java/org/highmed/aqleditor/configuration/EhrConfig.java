package org.highmed.aqleditor.configuration;

import java.net.URI;
import java.net.URISyntaxException;
import lombok.RequiredArgsConstructor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.ehrbase.openehr.sdk.client.openehrclient.OpenEhrClientConfig;
import org.ehrbase.openehr.sdk.client.openehrclient.defaultrestclient.DefaultRestClient;
import org.highmed.numportal.properties.EhrBaseProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class EhrConfig {

  private final EhrBaseProperties ehrBaseProperties;

  @Bean
  public DefaultRestClient createEhrRestClient() throws URISyntaxException {
    CredentialsProvider provider = new BasicCredentialsProvider();
    provider.setCredentials(
        AuthScope.ANY,
        new UsernamePasswordCredentials(
            ehrBaseProperties.getUsername(), ehrBaseProperties.getPassword()));

    CloseableHttpClient httpClient =
        HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();

    return new DefaultRestClient(
        new OpenEhrClientConfig(new URI(ehrBaseProperties.getRestApiUrl())), null, httpClient);
  }
}
