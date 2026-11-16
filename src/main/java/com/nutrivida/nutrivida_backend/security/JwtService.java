package com.nutrivida.nutrivida_backend.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.nutrivida.nutrivida_backend.config.JwtProperties;
import com.nutrivida.nutrivida_backend.model.Usuario;

// Emite el JWT después de un login correcto.
// Contenido: sub (id del usuario), rol, iss (emisor), aud (destinatario), iat y exp.
// No lleva contraseñas ni datos sensibles: el JWT se puede leer, solo su firma lo protege.
@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final JwtProperties props;

    public JwtService(JwtEncoder encoder, JwtProperties props) {
        this.encoder = encoder;
        this.props = props;
    }

    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.emisor())
                .audience(List.of(props.destinatario()))
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(ahora)
                .expiresAt(ahora.plus(props.duracionMinutos(), ChronoUnit.MINUTES))
                .claim("rol", usuario.getRol().getNombre())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    // Vigencia del token en segundos (se informa al frontend en la respuesta del login)
    public long segundosDeVigencia() {
        return props.duracionMinutos() * 60;
    }
}
