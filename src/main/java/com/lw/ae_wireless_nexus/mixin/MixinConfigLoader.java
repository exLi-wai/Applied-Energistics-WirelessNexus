package com.lw.ae_wireless_nexus.mixin;

import java.util.Collections;
import java.util.List;

import zone.rong.mixinbooter.ILateMixinLoader;
import zone.rong.mixinbooter.MixinLoader;

/** Registers the AE2-dependent mixins after AE2 classes are available. */
@MixinLoader
public final class MixinConfigLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("mixins.ae_wireless_nexus_late.json");
    }
}
