package org.highmed.numportal.web.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class ApplicationSecurity {

  private static final String[] AUTH_WHITELIST = {"/swagger-*/**", "/v2/**", "/v3/**", "/admin/health",
          "/admin/log-level", "/admin/log-level/*", "/admin/manuel-url", "/admin/services-status"};

    public class CustomSecurityFilter extends GenericFilterBean {
        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            logger.debug("LOG: request: " + request.getMethod() + " " + request.getRequestURI() + " client ip address:" + request.getRemoteAddr());
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable);
    httpSecurity
            .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(new AuthorizationConverter())))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(Customizer.withDefaults());
    httpSecurity.addFilterBefore(new CustomSecurityFilter(), SecurityContextPersistenceFilter.class);
    return httpSecurity.build();
  }
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) ->
                web.ignoring()
                        .requestMatchers(AUTH_WHITELIST)
                        .requestMatchers(HttpMethod.GET, "/content/navigation")
                        .requestMatchers(HttpMethod.GET, "/content/cards")
                        .requestMatchers(HttpMethod.GET, "/content/metrics")
                        .requestMatchers(HttpMethod.GET, "/actuator/health/**")
                        .requestMatchers(HttpMethod.GET, "/actuator/info**");
    }
}
