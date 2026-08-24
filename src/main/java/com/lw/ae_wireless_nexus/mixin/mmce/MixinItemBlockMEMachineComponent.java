package com.lw.ae_wireless_nexus.mixin.mmce;

import com.lw.ae_wireless_nexus.integration.mmce.MMCEInteractionHandler;
import hellfirepvp.modularmachinery.common.item.ItemBlockMEMachineComponent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Hooks MMCE's custom ItemBlock placement path. Generic RightClickBlock events
 * are not consistently emitted by ItemBlockMEMachineComponent. */
@Mixin(value = ItemBlockMEMachineComponent.class, remap = false)
public abstract class MixinItemBlockMEMachineComponent {
    @Inject(method = "placeBlockAt", at = @At("RETURN"), remap = false)
    private void aeWirelessNexus$afterPlace(ItemStack stack, EntityPlayer player, World world,
        BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState state,
        CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            MMCEInteractionHandler.bindPlacedComponent(world, pos, player);
        }
    }
}
