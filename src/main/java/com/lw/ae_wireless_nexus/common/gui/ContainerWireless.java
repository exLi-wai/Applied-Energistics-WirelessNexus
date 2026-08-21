package com.lw.ae_wireless_nexus.common.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public final class ContainerWireless extends Container {
    private final BlockPos position;

    public ContainerWireless(BlockPos position) {
        this.position = position;
    }

    public BlockPos getPosition() {
        return position;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        TileEntity tile = player.world.getTileEntity(position);
        return tile != null && player.getDistanceSq(position) <= 64.0D;
    }
}
