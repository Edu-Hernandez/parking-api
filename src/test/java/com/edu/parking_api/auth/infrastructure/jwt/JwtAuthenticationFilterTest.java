package com.edu.parking_api.auth.infrastructure.jwt;

import com.edu.parking_api.auth.application.GetCurrentUserUseCase;
import com.edu.parking_api.auth.domain.exception.InvalidAccessTokenException;
import com.edu.parking_api.auth.domain.model.AuthenticatedUser;
import com.edu.parking_api.auth.domain.port.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private final AuthenticatedUser user = new AuthenticatedUser(UUID.randomUUID(), "ana@example.com");

    private TokenProvider tokenProvider;
    private GetCurrentUserUseCase getCurrentUserUseCase;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        tokenProvider = mock(TokenProvider.class);
        getCurrentUserUseCase = mock(GetCurrentUserUseCase.class);
        filter = new JwtAuthenticationFilter(tokenProvider, getCurrentUserUseCase);

        when(tokenProvider.parse("valid")).thenReturn(user);
        when(tokenProvider.parse("invalid")).thenThrow(new InvalidAccessTokenException());
    }

    @Test
    void publicEndpointsDoNotRequireToken() throws Exception {
        List<String[]> publicEndpoints = List.of(
                new String[] {"GET", "/api/v1/health"},
                new String[] {"POST", "/api/v1/auth/login"},
                new String[] {"POST", "/api/v1/auth/refresh"},
                new String[] {"POST", "/api/v1/auth/logout"},
                new String[] {"POST", "/api/v1/users"});

        for (String[] endpoint : publicEndpoints) {
            MockFilterChain chain = run(endpoint[0], endpoint[1], null, new MockHttpServletResponse());
            assertNotNull(chain.getRequest(), endpoint[0] + " " + endpoint[1] + " debería ser público");
        }
    }

    @Test
    void publicEndpointsDependOnHttpMethod() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = run("GET", "/api/v1/users", null, response);

        assertNull(chain.getRequest());
        assertEquals(401, response.getStatus());
    }

    @Test
    void rejectsRequestWithoutAuthorizationHeader() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = run("GET", "/api/v1/auth/me", null, response);

        assertNull(chain.getRequest());
        assertEquals(401, response.getStatus());
        assertEquals("{\"message\":\"Token inválido o ausente\"}", response.getContentAsString());
    }

    @Test
    void rejectsEmptyBearerToken() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = run("GET", "/api/v1/auth/me", "Bearer   ", response);

        assertNull(chain.getRequest());
        assertEquals(401, response.getStatus());
    }

    @Test
    void rejectsInvalidToken() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = run("GET", "/api/v1/auth/me", "Bearer invalid", response);

        assertNull(chain.getRequest());
        assertEquals(401, response.getStatus());
    }

    @Test
    void rejectsValidTokenOfDeactivatedUser() throws Exception {
        when(getCurrentUserUseCase.execute(user.id())).thenThrow(new InvalidAccessTokenException());
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = run("GET", "/api/v1/auth/me", "Bearer valid", response);

        assertNull(chain.getRequest());
        assertEquals(401, response.getStatus());
    }

    @Test
    void acceptsValidTokenOfActiveUserAndExposesAuthenticatedUser() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = run("GET", "/api/v1/auth/me", "Bearer valid", response);

        assertNotNull(chain.getRequest());
        assertEquals(200, response.getStatus());
        assertEquals(user,
                chain.getRequest().getAttribute(JwtAuthenticationFilter.AUTHENTICATED_USER_ATTRIBUTE));
    }

    private MockFilterChain run(
            String method,
            String uri,
            String authorization,
            MockHttpServletResponse response) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        if (authorization != null) {
            request.addHeader("Authorization", authorization);
        }
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        return chain;
    }
}
