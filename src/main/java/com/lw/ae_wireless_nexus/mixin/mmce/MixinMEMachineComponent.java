package com.lw.ae_wireless_nexus.mixin.mmce;

import com.lw.ae_wireless_nexus.integration.mmce.IMMCEWirelessHost;
import com.lw.ae_wireless_nexus.integration.mmce.MMCEWirelessEndpoint;
import github.kasuminova.mmce.common.tile.base.MEMachineComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MEMachineComponent.class)
public abstract class MixinMEMachineComponent implements IMMCEWirelessHost {
    @Unique
    private MMCEWirelessEndpoint aeWirelessNexus$endpoint;

    @Override
    public MMCEWirelessEndpoint aeWirelessNexus$getWirelessEndpoint() {
        if (aeWirelessNexus$endpoint == null) {
            aeWirelessNexus$endpoint = new MMCEWirelessEndpoint((TileEntity) (Object) this);
        }
        return aeWirelessNexus$endpoint;
    }

    @Inject(method = "readCustomNBT", at = @At("RETURN"), remap = false)
    private void aeWirelessNexus$readWirelessData(NBTTagCompound tag, CallbackInfo ci) {
        if (tag.hasKey("AEWirelessNexusTarget") || tag.hasKey("AEWirelessNexusPlayer")) {
            aeWirelessNexus$getWirelessEndpoint().readFromNBT(tag);
        }
    }

    @Inject(method = "writeCustomNBT", at = @At("RETURN"), remap = false)
    private void aeWirelessNexus$writeWirelessData(NBTTagCompound tag, CallbackInfo ci) {
        if (aeWirelessNexus$endpoint != null || MMCEWirelessEndpoint.isSupported((TileEntity) (Object) this)) {
            aeWirelessNexus$getWirelessEndpoint().writeToNBT(tag);
        }
    }

    @Inject(method = {"validate", "func_145829_t"}, at = @At("RETURN"), remap = false)
    private void aeWirelessNexus$validateWirelessEndpoint(CallbackInfo ci) {
        if (MMCEWirelessEndpoint.isSupported((TileEntity) (Object) this)) {
            aeWirelessNexus$getWirelessEndpoint().validate();
        }
    }

    @Inject(method = "onChunkUnload", at = @At("HEAD"), remap = false)
    private void aeWirelessNexus$unloadWirelessEndpoint(CallbackInfo ci) {
        if (aeWirelessNexus$endpoint != null) aeWirelessNexus$endpoint.unload();
    }

    @Inject(method = {"invalidate", "func_145843_s"}, at = @At("HEAD"), remap = false)
    private void aeWirelessNexus$invalidateWirelessEndpoint(CallbackInfo ci) {
        if (aeWirelessNexus$endpoint != null) aeWirelessNexus$endpoint.unload();
    }
}
