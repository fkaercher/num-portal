package org.highmed.numportal.config;

import lombok.RequiredArgsConstructor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.ehrbase.openehr.sdk.client.openehrclient.OpenEhrClientConfig;
import org.ehrbase.openehr.sdk.client.openehrclient.defaultrestclient.DefaultRestClient;
import org.highmed.numportal.properties.AftProperties;
import org.highmed.numportal.properties.EhrBaseProperties;
import org.highmed.numportal.service.aft.AftRestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@RequiredArgsConstructor
public class EhrBaseConfig {

  private final EhrBaseProperties ehrBaseProperties;
  private final AftProperties aftProperties;

  @Bean
  public CommonsRequestLoggingFilter requestLoggingFilter() {
    CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
    loggingFilter.setIncludeClientInfo(true);
    loggingFilter.setIncludeQueryString(true);
    loggingFilter.setIncludePayload(true);
    loggingFilter.setMaxPayloadLength(64000);
    return loggingFilter;
  }
  @Bean
  @Primary
  public DefaultRestClient createRestClient() throws URISyntaxException {
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

  @Bean
  //@ConditionalOnProperty(value = "feature.aft", havingValue = "true")
  public AftRestClient createAftClient() throws URISyntaxException {
    CloseableHttpClient httpClient =
        HttpClientBuilder.create().build();

    return new AftRestClient(
        new OpenEhrClientConfig(new URI(aftProperties.getUrl())), null, httpClient);
  }
}
