package com.lw.ae_wireless_nexus.network;

import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;

public final class WirelessNetworkRecord {
    private final UUID id;
    private String name;
    private int dimension;
    private int x;
    private int y;
    private int z;
    private int totalChannels;
    private int allocatedChannels;
    private boolean online;

    public WirelessNetworkRecord(UUID id) {
        this.id = id;
        this.name = WirelessNetworkNames.defaultName(id);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = WirelessNetworkNames.sanitize(name);
    }

    public int getDimension() {
        return dimension;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
    public void setAnchor(int dimension, int x, int y, int z) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public int getTotalChannels() {
        return totalChannels;
    }
    public void setTotalChannels(int value) {
        totalChannels = Math.max(0, value);
    }
    public int getAllocatedChannels() {
        return allocatedChannels;
    }
    public void setAllocatedChannels(int value) {
        allocatedChannels = Math.max(0, value);
    }
    public boolean isOnline() {
        return online;
    }
    public void setOnline(boolean value) {
        online = value;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Id", id.toString());
        tag.setString("Name", name);
        tag.setInteger("Dimension", dimension);
        tag.setInteger("X", x);
        tag.setInteger("Y", y);
        tag.setInteger("Z", z);
        tag.setInteger("Total", totalChannels);
        tag.setInteger("Allocated", allocatedChannels);
        return tag;
    }

    public static WirelessNetworkRecord readFromNBT(NBTTagCompound tag) {
        try {
            UUID id = UUID.fromString(tag.getString("Id"));
            WirelessNetworkRecord record = new WirelessNetworkRecord(id);
            if (tag.hasKey("Name")) record.setName(tag.getString("Name"));
            record.setAnchor(
                    tag.getInteger("Dimension"),
                    tag.getInteger("X"),
                    tag.getInteger("Y"),
                    tag.getInteger("Z")
            );
            record.setTotalChannels(tag.getInteger("Total"));
            record.setAllocatedChannels(0);
            record.setOnline(false);
            return record;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
