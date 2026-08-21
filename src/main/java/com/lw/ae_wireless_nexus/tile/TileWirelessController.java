package com.lw.ae_wireless_nexus.tile;

import java.util.UUID;

import appeng.me.GridAccessException;
import appeng.tile.networking.TileController;
import net.minecraft.nbt.NBTTagCompound;

import com.lw.ae_wireless_nexus.ae_wireless_nexus;
import com.lw.ae_wireless_nexus.network.WirelessNetworkService;

public class TileWirelessController extends TileController {

    private UUID networkId = UUID.randomUUID();

    public UUID getNetworkId() {
        return networkId;
    }

    public String getNetworkName() {
        return WirelessNetworkService.getNetworkName(this);
    }

    public void setNetworkName(String value) {
        WirelessNetworkService.setNetworkName(this, value);
    }

    @Override
    public void onReady() {
        super.onReady();
        WirelessNetworkService.onControllerChanged(this);
        refreshVisualState();
        logGridState("ready");
    }

    @Override
    public void onNeighborChange(boolean force) {
        super.onNeighborChange(force);
        WirelessNetworkService.onControllerChanged(this);
        refreshVisualState();
        logGridState("neighbor");
    }

    private void refreshVisualState() {
        if (world != null) {
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    private void logGridState(String reason) {
        try {
            ae_wireless_nexus.LOGGER.info(
                "Wireless controller {} {}: node={}, state={}, powered={}",
                getNetworkId(),
                reason,
                getProxy().getNode() != null,
                getProxy().getPath().getControllerState(),
                getProxy().getEnergy().isNetworkPowered());
        } catch (GridAccessException ignored) {
            ae_wireless_nexus.LOGGER.info(
                "Wireless controller {} {}: AE2 grid unavailable",
                getNetworkId(),
                reason);
        }
    }

    @Override
    public void invalidate() {
        WirelessNetworkService.onControllerRemoved(this);
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        WirelessNetworkService.onControllerUnloaded(this);
        super.onChunkUnload();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("WirelessNetworkId")) {
            try {
                networkId = UUID.fromString(tag.getString("WirelessNetworkId"));
            } catch (IllegalArgumentException ignored) {
                // Keep the generated UUID when saved data is invalid.
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setString("WirelessNetworkId", networkId.toString());
        return tag;
    }
}
