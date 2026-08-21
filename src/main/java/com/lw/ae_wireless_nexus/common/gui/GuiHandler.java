package com.lw.ae_wireless_nexus.common.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import com.lw.ae_wireless_nexus.client.gui.GuiWireless;
import com.lw.ae_wireless_nexus.common.network.PacketWirelessNetworks;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import com.lw.ae_wireless_nexus.common.network.PacketWirelessState;

public final class GuiHandler implements IGuiHandler {
    public static final int WIRELESS_CONTROLLER = 1;
    public static final int WIRELESS_CONNECTOR = 2;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity tile = world.getTileEntity(pos);
        if ((id == WIRELESS_CONTROLLER && !(tile instanceof com.lw.ae_wireless_nexus.tile.TileWirelessController)) ||
            (id == WIRELESS_CONNECTOR && !(tile instanceof com.lw.ae_wireless_nexus.tile.TileWirelessConnector))) return null;
        if (player instanceof EntityPlayerMP) {
            PacketWirelessNetworks.send((EntityPlayerMP) player);
            PacketWirelessState.send((EntityPlayerMP) player, pos, id);
        }
        return new ContainerWireless(pos);
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return new GuiWireless(
            new ContainerWireless(new net.minecraft.util.math.BlockPos(x, y, z)),
            id,
            new net.minecraft.util.math.BlockPos(x, y, z));
    }
}
