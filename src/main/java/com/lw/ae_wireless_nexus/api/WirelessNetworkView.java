package com.lw.ae_wireless_nexus.api;

import java.util.UUID;

public final class WirelessNetworkView {
    private final UUID id;
    private final String name;
    private final int totalChannels;
    private final int allocatedChannels;
    private final boolean online;

    public WirelessNetworkView(UUID id, String name, int totalChannels, int allocatedChannels, boolean online) {
        this.id = id;
        this.name = name;
        this.totalChannels = totalChannels;
        this.allocatedChannels = allocatedChannels;
        this.online = online;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public int getTotalChannels() { return totalChannels; }
    public int getAllocatedChannels() { return allocatedChannels; }
    public int getAvailableChannels() { return Math.max(0, totalChannels - allocatedChannels); }
    public boolean isOnline() { return online; }
}
