package de.vitagroup.num.web.config;

import java.util.Collection;
import java.util.stream.Collectors;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private static final String ROLE_PREFIX = "ROLE_";
  private static final String REALM_ACCESS = "realm_access";
  private static final String ROLES_CLAIM = "roles";

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities = this.extractAuthorities(jwt);
    return new JwtAuthenticationToken(jwt, authorities);
  }

  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    JSONObject realmAccess = jwt.getClaim(REALM_ACCESS);
    if (realmAccess != null) {
      final JSONArray roles = (JSONArray) realmAccess.get(ROLES_CLAIM);

      if (CollectionUtils.isNotEmpty(roles)) {
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
            .collect(Collectors.toSet());
      }
    }
    return SetUtils.emptySet();
  }
}
