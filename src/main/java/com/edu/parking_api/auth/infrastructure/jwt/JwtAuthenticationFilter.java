package com.edu.parking_api.auth.infrastructure.jwt;

import com.edu.parking_api.auth.domain.AuthenticatedUser;
import com.edu.parking_api.auth.domain.exception.InvalidAccessTokenException;
import com.edu.parking_api.auth.domain.port.TokenProvider;
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

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHENTICATED_USER_ATTRIBUTE = "authenticatedUser";

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String UNAUTHORIZED_BODY = "{\"message\":\"Token inválido o ausente\"}";

    private final TokenProvider tokenProvider;

    public JwtAuthenticationFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
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

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            writeUnauthorized(response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            writeUnauthorized(response);
            return;
        }

        try {
            AuthenticatedUser user = tokenProvider.parse(token);
            request.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, user);
            filterChain.doFilter(request, response);
        } catch (InvalidAccessTokenException ex) {
            writeUnauthorized(response);
        }
    }

    private boolean isPublic(HttpServletRequest request) {
        String path = path(request);
        String method = request.getMethod();
        return ("GET".equals(method) && "/api/v1/health".equals(path))
                || ("POST".equals(method) && "/api/v1/auth/login".equals(path))
                || ("POST".equals(method) && "/api/v1/users".equals(path));
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
