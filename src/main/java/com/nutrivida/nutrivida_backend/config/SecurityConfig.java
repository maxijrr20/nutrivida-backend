package com.nutrivida.nutrivida_backend.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nutrivida.nutrivida_backend.security.RespuestasSeguridad;

// Configuración de seguridad:
// - Qué rutas son públicas y cuáles exigen un JWT válido.
// - Cómo se firman y verifican los JWT (HS256 + emisor + destinatario + vigencia).
// - Cómo se guardan las contraseñas (BCrypt).
// - Qué orígenes del frontend pueden llamar a la API (CORS).
@Configuration
@EnableMethodSecurity // habilita @PreAuthorize("hasRole('ADMIN')") en los controladores
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, RespuestasSeguridad respuestas) throws Exception {
        http
            // API sin sesión en el servidor: cada solicitud trae su token, por eso no aplica CSRF
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> {})
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login").permitAll()
                // Catálogo público: las páginas del sitio lo muestran sin iniciar sesión (solo lectura)
                .requestMatchers(HttpMethod.GET, "/api/v1/servicios/**", "/api/v1/categorias/**",
                        "/api/v1/nutricionistas/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated())
            // Valida el header "Authorization: Bearer <token>" en cada solicitud protegida
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(convertidorDeRoles()))
                .authenticationEntryPoint(respuestas)
                .accessDeniedHandler(respuestas))
            .exceptionHandling(e -> e
                .authenticationEntryPoint(respuestas)
                .accessDeniedHandler(respuestas));
        return http.build();
    }

    // BCrypt con costo 12: cada hash lleva su propia sal y es lento a propósito
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    SecretKey claveJwt(JwtProperties props) {
        return new SecretKeySpec(props.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey claveJwt) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(claveJwt));
    }

    // Verificación del token: firma con HS256 (único algoritmo aceptado), vigencia (exp),
    // emisor (iss) y destinatario (aud). Decodificarlo no basta: si algo falla, responde 401.
    @Bean
    JwtDecoder jwtDecoder(SecretKey claveJwt, JwtProperties props) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(claveJwt)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        OAuth2TokenValidator<Jwt> emisorYVigencia = JwtValidators.createDefaultWithIssuer(props.emisor());
        OAuth2TokenValidator<Jwt> destinatario = new JwtClaimValidator<List<String>>(
                "aud", aud -> aud != null && aud.contains(props.destinatario()));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(emisorYVigencia, destinatario));
        return decoder;
    }

    // El claim "rol" del token (ADMIN, NUTRICIONISTA, PACIENTE) se convierte en ROLE_ADMIN, etc.
    private JwtAuthenticationConverter convertidorDeRoles() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("rol");
        roles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(roles);
        return convertidor;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.origenes-permitidos}") List<String> origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origenes);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
