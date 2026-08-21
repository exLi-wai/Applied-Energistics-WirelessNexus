package com.lw.ae_wireless_nexus.mixin.ae2;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IMachineSet;
import appeng.me.Grid;
import appeng.me.MachineSet;
import appeng.tile.networking.TileController;
import com.lw.ae_wireless_nexus.tile.TileWirelessController;

@Mixin(value = Grid.class, remap = false)
public abstract class MixinGrid {

    @Shadow
    @Final
    private Map<Class<? extends IGridHost>, MachineSet> machines;

    @Inject(method = "getMachines", at = @At("RETURN"), cancellable = true)
    private void aeWirelessNexus$includeWirelessControllers(Class<? extends IGridHost> machineClass,
        CallbackInfoReturnable<IMachineSet> cir) {
        if (machineClass != TileController.class) return;
        MachineSet wireless = machines.get(TileWirelessController.class);
        if (wireless == null || wireless.isEmpty()) return;
        if (!(cir.getReturnValue() instanceof MachineSet)) return;
        MachineSet controllers = (MachineSet) cir.getReturnValue();
        controllers.addAll(wireless);
        cir.setReturnValue(controllers);
    }
}
