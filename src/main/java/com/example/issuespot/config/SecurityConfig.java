package com.example.issuespot.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
public class SecurityConfig {
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            /* WHY PERMIT ALL FOR THESE ENDPOINTS:
             * /api/v1/auth/** : Needed for unauthenticated users to request/verify OTPs to login.
             * /api/v1/posts/** (GET) : The feed is public. We want users to see issues without logging in.
             * Any mutating actions (POST/PUT/DELETE) on posts still require authentication.
             */
            .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/s3-v2-test").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/posts/**").permitAll()
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
    return http.build();
  }
}
