package org.monitor.tests;

import org.junit.Test;
import org.monitor.exceptions.ImageDownloadException;
import org.monitor.server.HTTPOperations;

import java.net.MalformedURLException;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.*;

public class MonitorServerTests {

    @Test
    public void testIfSiteIsUpFunction() throws MalformedURLException {
        assertTrue(HTTPOperations.isSiteUp("https://www.google.com"));
    }

    @Test
    public void testIfSiteIsUpWhenDown() throws MalformedURLException {
        assertFalse(HTTPOperations.isSiteUp("http://ksaoigjaoeijgoiajigf.pl/"));
    }
}
