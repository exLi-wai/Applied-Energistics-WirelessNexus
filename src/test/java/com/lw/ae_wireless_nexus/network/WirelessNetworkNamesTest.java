package com.lw.ae_wireless_nexus.network;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WirelessNetworkNamesTest {

    @Test
    public void stripsControlCharactersAndTrims() {
        assertEquals("AlphaBeta", WirelessNetworkNames.sanitize("  Alpha\nBeta\t"));
    }

    @Test
    public void defaultNameContainsStablePrefix() {
        assertTrue(WirelessNetworkNames.defaultName(java.util.UUID.randomUUID()).startsWith("ME Wireless Network "));
    }
}
