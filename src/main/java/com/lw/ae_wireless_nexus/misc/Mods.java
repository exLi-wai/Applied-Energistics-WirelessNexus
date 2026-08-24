package com.lw.ae_wireless_nexus.misc;

import net.minecraftforge.fml.common.Loader;

public enum Mods {
    MMCE("modularmachinery");

    public final String modid;

    Mods(String modid) {
        this.modid = modid;
    }

    public boolean isLoaded() {
        return Loader.isModLoaded(this.modid);
    }
}
