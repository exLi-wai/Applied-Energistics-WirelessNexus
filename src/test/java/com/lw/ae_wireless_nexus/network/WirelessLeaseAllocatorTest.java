package com.lw.ae_wireless_nexus.network;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class WirelessLeaseAllocatorTest {

    private static final class R implements WirelessLeaseAllocator.Request {
        final String key;
        final int priority;
        final int channels;

        R(String key, int priority, int channels) {
            this.key = key;
            this.priority = priority;
            this.channels = channels;
        }
        public int getPriority() {
            return priority;
        }
        public int getChannels() {
            return channels;
        }
        public String getStableKey() {
            return key;
        }
    }

    @Test
    public void grantsHighestPriorityFirst() {
        Set<String> granted = WirelessLeaseAllocator.allocate(2, Arrays.asList(new R("low", 1, 1), new R("high", 2, 2)));
        assertEquals(Collections.singleton("high"), granted);
    }

    @Test
    public void stableKeyBreaksPriorityTie() {
        Set<String> granted = WirelessLeaseAllocator.allocate(1, Arrays.asList(new R("b", 1, 1), new R("a", 1, 1)));
        assertEquals(Collections.singleton("a"), granted);
    }
}
