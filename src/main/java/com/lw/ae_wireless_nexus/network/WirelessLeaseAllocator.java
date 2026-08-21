package com.lw.ae_wireless_nexus.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class WirelessLeaseAllocator {
    private WirelessLeaseAllocator() {}

    public static Set<String> allocate(int capacity, Collection<? extends Request> requests) {
        List<Request> sorted = new ArrayList<Request>(requests);
        sorted.sort((a, b) -> {
            int priority = Integer.compare(b.getPriority(), a.getPriority());
            return priority != 0 ? priority : a.getStableKey().compareTo(b.getStableKey());
        });
        int remaining = Math.max(0, capacity);
        Set<String> granted = new HashSet<String>();
        for (Request request : sorted) {
            int channels = Math.max(0, request.getChannels());
            if (channels <= remaining) {
                granted.add(request.getStableKey());
                remaining -= channels;
            }
        }
        return granted;
    }

    public interface Request {
        int getPriority();
        int getChannels();
        String getStableKey();
    }
}
