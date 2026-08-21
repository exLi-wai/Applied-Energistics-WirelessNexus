package com.lw.ae_wireless_nexus.network;

import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WirelessNetworkRecordTest {
    @Test
    public void nbtRoundTripPreservesPersistentFields() {
        UUID id = UUID.randomUUID();
        WirelessNetworkRecord original = new WirelessNetworkRecord(id);
        original.setName("Test Network");
        original.setAnchor(7, 10, 64, -12);
        original.setTotalChannels(96);
        original.setAllocatedChannels(32);
        original.setOnline(true);
        WirelessNetworkRecord restored = WirelessNetworkRecord.readFromNBT(original.writeToNBT());
        assertNotNull(restored);
        assertEquals(id, restored.getId());
        assertEquals("Test Network", restored.getName());
        assertEquals(7, restored.getDimension());
        assertEquals(10, restored.getX());
        assertEquals(64, restored.getY());
        assertEquals(-12, restored.getZ());
        assertEquals(96, restored.getTotalChannels());
        assertEquals(0, restored.getAllocatedChannels());
        assertFalse(restored.isOnline());
    }

    @Test
    public void invalidUuidIsRejected() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Id", "invalid");
        assertNull(WirelessNetworkRecord.readFromNBT(tag));
    }
}
