package com.xtremand.emailverification.service.verifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SyntaxCheckProviderTest {

    private SyntaxCheckProvider syntaxCheckProvider;

    @BeforeEach
    void setUp() {
        syntaxCheckProvider = new SyntaxCheckProvider();
    }

    @Test
    void testValidEmail() {
        assertTrue(syntaxCheckProvider.isValid("test@example.com"));
        assertTrue(syntaxCheckProvider.isValid("test.name@example.co.uk"));
    }

    @Test
    void testInvalidEmail() {
        assertFalse(syntaxCheckProvider.isValid("test@.com"));
        assertFalse(syntaxCheckProvider.isValid("test@com."));
        assertFalse(syntaxCheckProvider.isValid("test@.com."));
        assertFalse(syntaxCheckProvider.isValid("test@comcom"));
        assertFalse(syntaxCheckProvider.isValid("test"));
        assertFalse(syntaxCheckProvider.isValid("@example.com"));
    }

    @Test
    void testNullAndEmptyEmail() {
        assertFalse(syntaxCheckProvider.isValid(null));
        assertFalse(syntaxCheckProvider.isValid(""));
        assertFalse(syntaxCheckProvider.isValid("   "));
    }
}