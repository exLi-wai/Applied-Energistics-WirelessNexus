package com.lw.ae_wireless_nexus.common.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import com.lw.ae_wireless_nexus.network.WirelessEndpointGuiService;
import com.lw.ae_wireless_nexus.tile.TileWirelessConnector;
import com.lw.ae_wireless_nexus.tile.TileWirelessController;

public final class ContainerWireless extends Container {
    private final BlockPos position;
    private final int guiId;

    public ContainerWireless(int guiId, BlockPos position) {
        this.guiId = guiId;
        this.position = position;
    }

    public BlockPos getPosition() {
        return position;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        TileEntity tile = player.world.getTileEntity(position);
        if (tile == null || player.getDistanceSq(position) > WirelessEndpointGuiService.MAX_DISTANCE_SQ) return false;
        if (guiId == GuiHandler.WIRELESS_CONTROLLER) return tile instanceof TileWirelessController;
        if (guiId == GuiHandler.WIRELESS_CONNECTOR) return tile instanceof TileWirelessConnector;
        return guiId == GuiHandler.WIRELESS_ENDPOINT
            && WirelessEndpointGuiService.isEndpointAt(player.world, position);
    }
}
