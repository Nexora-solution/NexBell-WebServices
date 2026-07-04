package com.nexora.nexora_web_service.iam.infrastructure.security.config;

import com.nexora.nexora_web_service.iam.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public WebSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // CORS preflight: never authenticate OPTIONS requests.
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // --- Public: authentication & password recovery ---
                .requestMatchers("/api/iam/register", "/api/iam/login", "/api/iam/refresh", "/api/iam/password/**").permitAll()

                // --- Public: onboarding (contract provisioning + credential claim + mobile lookups) ---
                .requestMatchers("/api/onboarding/**").permitAll()

                // --- Public: building catalog (used in pre-login building selector, no token yet) ---
                .requestMatchers(HttpMethod.GET, "/api/directory/buildings").permitAll()

                // --- Public: API docs ---
                .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                // --- Public: IoT hardware ingress (edge/ESP32 -> backend, no JWT). DO NOT change without updating the edge. ---
                .requestMatchers(HttpMethod.POST, "/api/security/iot/presence").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/security/iot/door-state").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/security/alarms/tampering").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/security/face/event").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/intercom/visit-requests").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/intercom/visit-requests/*/evidence").permitAll()

                // --- Public: SSE / video streams (browser EventSource cannot send the Authorization header) ---
                .requestMatchers(HttpMethod.GET, "/api/intercom/queue/stream").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/intercom/visit-requests/*/stream").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/intercom/video-stream", "/api/intercom/video-stream/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/security/alarms/stream").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/security/face/stream").permitAll()

                // --- Public: WebSocket Media Hub (no authentication for local/prototype) ---
                .requestMatchers("/ws/media", "/ws/media/**").permitAll()

                // --- Everything else requires a valid JWT ---
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        var configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOriginPatterns(java.util.List.of("*"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "Cache-Control"));
        configuration.setAllowCredentials(true);
        var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
