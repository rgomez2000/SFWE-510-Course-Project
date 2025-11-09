package com.example.catalog.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.*;

@Configuration
class SecurityConfig {

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health","/actuator/info").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/**").hasAnyRole("USER","ADMIN")
                .requestMatchers(HttpMethod.POST, "/v1/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,  "/v1/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE,"/v1/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        );
        http.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter())));
        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthConverter() {
        var c = new JwtAuthenticationConverter();
        c.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return c;
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        var out = new HashSet<GrantedAuthority>();
        var realm = (Map<String, Object>) jwt.getClaim("realm_access");
        if (realm != null) {
            var roles = (Collection<String>) realm.get("roles");
            if (roles != null) roles.forEach(r -> out.add(new SimpleGrantedAuthority("ROLE_" + r)));
        }
        var res = (Map<String, Object>) jwt.getClaim("resource_access");
        if (res != null) for (var v : res.values()) {
            var m = (Map<String, Object>) v;
            var roles = (Collection<String>) m.get("roles");
            if (roles != null) roles.forEach(r -> out.add(new SimpleGrantedAuthority("ROLE_" + r)));
        }
        return out;
    }
}
