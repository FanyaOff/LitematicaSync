package com.fanya.litematicasync.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ServerGroup {
    private String name;
    private List<String> addresses;
    private boolean enabled;

    public ServerGroup(String name, List<String> addresses) {
        this.name = name;
        this.addresses = new ArrayList<>(addresses);
        this.enabled = true;
    }

    public String name() {
        return this.name == null ? "" : this.name.trim();
    }

    public List<String> addresses() {
        if (this.addresses == null) {
            this.addresses = new ArrayList<>();
        }

        return this.addresses;
    }

    public boolean enabled() {
        return this.enabled;
    }

    public boolean containsAddress(String address) {
        if (!this.enabled || address == null || address.isBlank()) {
            return false;
        }

        String normalized = normalizeAddress(address);

        for (String configuredAddress : this.addresses()) {
            if (normalizeAddress(configuredAddress).equals(normalized)) {
                return true;
            }
        }

        return false;
    }

    public String toEditableLine() {
        return this.name() + "=" + String.join(",", this.addresses());
    }

    public static ServerGroup fromEditableLine(String line) {
        String[] parts = line.split("=", 2);
        String name = parts.length > 0 ? parts[0].trim() : "";
        List<String> addresses = new ArrayList<>();

        if (parts.length == 2) {
            for (String address : parts[1].split(",")) {
                String trimmed = address.trim();

                if (!trimmed.isEmpty()) {
                    addresses.add(trimmed);
                }
            }
        }

        return new ServerGroup(name, addresses);
    }

    public static String normalizeAddress(String address) {
        String normalized = address.trim().toLowerCase(Locale.ROOT);
        int schemeIndex = normalized.indexOf("://");

        if (schemeIndex >= 0) {
            normalized = normalized.substring(schemeIndex + 3);
        }

        int slashIndex = normalized.indexOf('/');

        if (slashIndex >= 0) {
            normalized = normalized.substring(0, slashIndex);
        }

        if (normalized.endsWith(":25565")) {
            normalized = normalized.substring(0, normalized.length() - ":25565".length());
        }

        return normalized;
    }
}
