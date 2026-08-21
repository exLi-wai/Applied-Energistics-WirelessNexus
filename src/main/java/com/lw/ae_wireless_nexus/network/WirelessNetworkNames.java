package com.lw.ae_wireless_nexus.network;

import java.util.UUID;

public final class WirelessNetworkNames {
    public static final int MAX_LENGTH = 32;

    private WirelessNetworkNames() {}

    public static String sanitize(String value) {
        if (value == null) return "";
        StringBuilder result = new StringBuilder();
        int codePoints = 0;
        int limit = Math.max(1, com.lw.ae_wireless_nexus.config.WirelessConfig.maxNetworkNameLength);
        for (int offset = 0; offset < value.length() && codePoints < limit;) {
            int codePoint = value.codePointAt(offset);
            offset += Character.charCount(codePoint);
            if (Character.isISOControl(codePoint) || codePoint == '\n' || codePoint == '\r') continue;
            result.appendCodePoint(codePoint);
            codePoints++;
        }
        return result.toString().trim();
    }

    public static String defaultName(UUID id) {
        return "ME Wireless Network " + id.toString().substring(0, 8);
    }
}
