package com.lw.ae_wireless_nexus.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;

import com.lw.ae_wireless_nexus.ae_wireless_nexus;

public final class WirelessNetworkSavedData extends WorldSavedData {
    private static final String KEY = ae_wireless_nexus.MOD_ID + ".networks";
    private static final int DATA_VERSION = 1;
    private final Map<UUID, WirelessNetworkRecord> records = new HashMap<UUID, WirelessNetworkRecord>();

    public WirelessNetworkSavedData() {
        super(KEY);
    }
    public WirelessNetworkSavedData(String name) {
        super(name);
    }

    public static WirelessNetworkSavedData get(World world) {
        if (world == null) return null;
        World primary = DimensionManager.getWorld(0);
        MapStorage storage = (primary == null ? world : primary).getMapStorage();
        WirelessNetworkSavedData data = (WirelessNetworkSavedData) storage.getOrLoadData(WirelessNetworkSavedData.class, KEY);
        if (data == null) {
            data = new WirelessNetworkSavedData();
            storage.setData(KEY, data);
        }
        return data;
    }

    public WirelessNetworkRecord getOrCreate(UUID id) {
        WirelessNetworkRecord record = records.get(id);
        if (record == null) { record = new WirelessNetworkRecord(id); records.put(id, record); markDirty(); }
        return record;
    }
    public WirelessNetworkRecord get(UUID id) {
        return records.get(id);
    }
    public Collection<WirelessNetworkRecord> records() {
        return Collections.unmodifiableCollection(new ArrayList<WirelessNetworkRecord>(records.values()));
    }
    public void remove(UUID id) { if (records.remove(id) != null) markDirty(); }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        records.clear();
        int version = tag.hasKey("Version") ? tag.getInteger("Version") : 0;
        NBTTagList list = tag.getTagList("Networks", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            WirelessNetworkRecord record = WirelessNetworkRecord.readFromNBT(list.getCompoundTagAt(i));
            if (record != null) records.put(record.getId(), record);
        }
        if (version < DATA_VERSION) markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (WirelessNetworkRecord record : records.values()) list.appendTag(record.writeToNBT());
        tag.setTag("Networks", list);
        tag.setInteger("Version", DATA_VERSION);
        return tag;
    }
}
