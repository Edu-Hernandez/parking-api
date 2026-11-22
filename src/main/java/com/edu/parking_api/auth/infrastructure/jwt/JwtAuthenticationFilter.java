package com.edu.parking_api.auth.infrastructure.jwt;

import com.edu.parking_api.auth.application.GetCurrentUserUseCase;
import com.edu.parking_api.auth.domain.exception.InvalidAccessTokenException;
import com.edu.parking_api.auth.domain.model.AuthenticatedUser;
import com.edu.parking_api.auth.domain.port.TokenProvider;
import com.edu.parking_api.config.WebConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHENTICATED_USER_ATTRIBUTE = "authenticatedUser";

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String UNAUTHORIZED_BODY =
            "{\"message\":\"" + InvalidAccessTokenException.MESSAGE + "\"}";

    // Endpoints que no exigen access token, con el formato "MÉTODO ruta".
    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "GET " + WebConfig.API_PREFIX + "/health",
            "POST " + WebConfig.API_PREFIX + "/auth/login",
            "POST " + WebConfig.API_PREFIX + "/auth/refresh",
            "POST " + WebConfig.API_PREFIX + "/auth/logout",
            "POST " + WebConfig.API_PREFIX + "/users"
    );

    private final TokenProvider tokenProvider;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    public JwtAuthenticationFilter(
            TokenProvider tokenProvider,
            GetCurrentUserUseCase getCurrentUserUseCase) {
        this.tokenProvider = tokenProvider;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (isPublic(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractBearerToken(request);
        if (token == null) {
            writeUnauthorized(response);
            return;
        }

        try {
            AuthenticatedUser user = tokenProvider.parse(token);
            // Rechaza usuarios desactivados aunque el JWT siga vigente.
            // Lanza InvalidAccessTokenException si no existe o está inactivo.
            getCurrentUserUseCase.execute(user.id());
            request.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, user);
            filterChain.doFilter(request, response);
        } catch (InvalidAccessTokenException ex) {
            writeUnauthorized(response);
        }
    }

    private boolean isPublic(HttpServletRequest request) {
        return PUBLIC_ENDPOINTS.contains(request.getMethod() + " " + path(request));
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private String path(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            return uri.substring(contextPath.length());
        }
        return uri;
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(UNAUTHORIZED_BODY);
    }
}
