package com.lw.ae_wireless_nexus.mixin.ae2;

import java.util.Set;

import com.lw.ae_wireless_nexus.ae_wireless_nexus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import appeng.api.networking.IGrid;
import appeng.api.networking.events.MENetworkControllerChange;
import appeng.api.networking.pathing.ControllerState;
import appeng.me.cache.PathGridCache;
import appeng.tile.networking.TileController;
import com.lw.ae_wireless_nexus.network.WirelessNetworkService;

@Mixin(value = PathGridCache.class, remap = false)
public abstract class MixinPathGridCache {

    @Final
    @Shadow
    private Set<TileController> controllers;

    @Final
    @Shadow
    private IGrid myGrid;

    @Shadow
    private ControllerState controllerState;

    @Inject(method = "recalcController", at = @At("RETURN"))
    private void aeWirelessNexus$rejectMultipleWirelessControllers(CallbackInfo ci) {
        ae_wireless_nexus.LOGGER.debug(
            "MixinPathGridCache recalculated controller set (size={})", controllers.size());
        if (WirelessNetworkService.hasMultipleWirelessControllers(controllers)
            && controllerState != ControllerState.CONTROLLER_CONFLICT) {
            controllerState = ControllerState.CONTROLLER_CONFLICT;
            myGrid.postEvent(new MENetworkControllerChange());
        }
    }
}
