package com.lw.ae_wireless_nexus.integration.baubles;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;

import com.lw.ae_wireless_nexus.network.WirelessNetworkToolBinding;

public final class BaublesWirelessToolSupport {

    private BaublesWirelessToolSupport() {}

    @Optional.Method(modid = "baubles")
    public static ItemStack findBoundWirelessTool(EntityPlayer player) {
        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
        if (handler == null) return ItemStack.EMPTY;

        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (WirelessNetworkToolBinding.hasBinding(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
