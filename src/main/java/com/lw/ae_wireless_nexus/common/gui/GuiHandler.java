package com.lw.ae_wireless_nexus.common.gui;

import com.lw.ae_wireless_nexus.tile.TileWirelessConnector;
import com.lw.ae_wireless_nexus.tile.TileWirelessController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import com.lw.ae_wireless_nexus.client.gui.GuiWireless;
import com.lw.ae_wireless_nexus.common.network.PacketWirelessNetworks;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import com.lw.ae_wireless_nexus.common.network.PacketWirelessState;
import com.lw.ae_wireless_nexus.network.WirelessEndpointLookup;
import com.lw.ae_wireless_nexus.network.WirelessEndpointGuiService;

public final class GuiHandler implements IGuiHandler {
    public static final int WIRELESS_CONTROLLER = 1;
    public static final int WIRELESS_CONNECTOR = 2;
    public static final int WIRELESS_ENDPOINT = 3;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity tile = world.getTileEntity(pos);
        if ((id == WIRELESS_CONTROLLER && !(tile instanceof TileWirelessController))
            || (id == WIRELESS_CONNECTOR && !(tile instanceof TileWirelessConnector))
            || (id == WIRELESS_ENDPOINT && !WirelessEndpointGuiService.isEndpointAt(world, pos))) return null;
        if (player instanceof EntityPlayerMP) {
            PacketWirelessNetworks.send((EntityPlayerMP) player);
            PacketWirelessState.send((EntityPlayerMP) player, pos, id);
        }
        return new ContainerWireless(id, pos);
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return new GuiWireless(
            new ContainerWireless(id, new BlockPos(x, y, z)),
            id,
            new BlockPos(x, y, z));
    }
}
