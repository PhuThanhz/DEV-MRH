package vn.system.app.common.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

class SecurityConfigurationTest {

    private final BearerTokenResolver resolver = new SecurityConfiguration().bearerTokenResolver();

    @Test
    void ignoresInvalidBearerTokenOnLogoutEndpoint() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/logout");
        request.setServletPath("/api/v1/auth/logout");
        request.addHeader("Authorization", "Bearer expired-or-invalid-token");

        assertNull(resolver.resolve(request));
    }

    @Test
    void resolvesBearerTokenOnProtectedEndpoints() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/account");
        request.setServletPath("/api/v1/auth/account");
        request.addHeader("Authorization", "Bearer access-token");

        assertEquals("access-token", resolver.resolve(request));
    }
}
