package com.lw.ae_wireless_nexus.mixin;

import java.util.ArrayList;
import java.util.List;
import com.lw.ae_wireless_nexus.misc.Mods;

import zone.rong.mixinbooter.ILateMixinLoader;

public final class MixinConfigLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        List<String> configs = new ArrayList<String>();
        configs.add("mixins.ae_wireless_nexus_late.json");
        if (Mods.MMCE.isLoaded()) configs.add("mixins.ae_wireless_nexus_mmce.json");
        return configs;
    }
}
