package com.lw.ae_wireless_nexus.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

class WirelessLocationTest {
    @Test
    void preservesDimensionAndPosition() {
        WirelessLocation location = new WirelessLocation(7, new BlockPos(12, 34, -56));
        assertEquals(7, location.getDimension());
        assertEquals(new BlockPos(12, 34, -56), location.getBlockPos());
        assertEquals("7:12:34:-56", location.toString());
    }

    @Test
    void dimensionIsPartOfIdentity() {
        WirelessLocation overworld = new WirelessLocation(0, 1, 2, 3);
        assertEquals(overworld, new WirelessLocation(0, 1, 2, 3));
        assertNotEquals(overworld, new WirelessLocation(1, 1, 2, 3));
    }
}
