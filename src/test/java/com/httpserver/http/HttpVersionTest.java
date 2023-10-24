package com.httpserver.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpVersionTest {

    @Test
    void getBestCompatibleVersionTest() {
        HttpVersion version = null;
        try {
            version = HttpVersion.getBestCompatibleVersion("HTTP/1.1");
        } catch (InvalidHttpVersionException e) {
            fail();
        }
        assertNotNull(version);
        assertEquals(version, HttpVersion.HTTP_1_1);
    }

    @Test
    void getBestCompatibleVersionBadFormatTest() {
        HttpVersion version = null;
        try {
            version = HttpVersion.getBestCompatibleVersion("http/1.1");
            fail();
        } catch (InvalidHttpVersionException e) {

        }
    }

    @Test
    void getBestCompatibleVersionHigherVersionTest() {
        HttpVersion version = null;
        try {
            version = HttpVersion.getBestCompatibleVersion("HTTP/1.2");
        } catch (InvalidHttpVersionException e) {
            fail();
        }
        assertNotNull(version);
        assertEquals(version, HttpVersion.HTTP_1_1);
    }
}