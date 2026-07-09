package com.news2026;

import com.news2026.util.JwtUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTests {

    @Test
    void testGenerateAndVerifyToken() {
        String username = "testuser";
        String role = "USER";
        
        String token = JwtUtil.generateToken(username, role);
        assertNotNull(token);
        assertTrue(token.length() > 0);

        String decodedUsername = JwtUtil.verifyTokenAndGetUsername(token);
        assertEquals(username, decodedUsername);
    }

    @Test
    void testVerifyInvalidToken() {
        String invalidToken = "invalid.token.value";
        String decodedUsername = JwtUtil.verifyTokenAndGetUsername(invalidToken);
        assertNull(decodedUsername);
    }
}
