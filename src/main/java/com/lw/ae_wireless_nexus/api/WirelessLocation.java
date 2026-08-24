package com.lw.ae_wireless_nexus.api;

import net.minecraft.util.math.BlockPos;

/** Immutable dimension and block position used by the public adapter API. */
public final class WirelessLocation {
    private final int dimension;
    private final int x;
    private final int y;
    private final int z;

    public WirelessLocation(int dimension, int x, int y, int z) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public WirelessLocation(int dimension, BlockPos position) {
        this(dimension, position.getX(), position.getY(), position.getZ());
    }

    public int getDimension() { return dimension; }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getZ() {
        return z;
    }
    public BlockPos getBlockPos() {
        return new BlockPos(x, y, z);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof WirelessLocation)) return false;
        WirelessLocation other = (WirelessLocation) object;
        return dimension == other.dimension && x == other.x && y == other.y && z == other.z;
    }

    @Override
    public int hashCode() {
        int result = dimension;
        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }

    @Override
    public String toString() {
        return dimension + ":" + x + ":" + y + ":" + z;
    }
}
