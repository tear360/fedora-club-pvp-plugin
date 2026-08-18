package fr.duelplugin.models;

public enum DuelGameMode {

    SWORD("Sword", "§6⚔ Sword", "sword", true, false, false),
    AXE("Axe", "§6🪓 Axe", "axe", true, false, false),
    UHC("UHC", "§6❤ UHC", "uhc", true, false, false),
    POT("Pot", "§6🧪 Pot", "pot", true, false, false),
    NETHPOT("NethPot", "§6🔥 NethPot", "nethpot", true, false, false),
    MACE("Mace", "§6🔨 Mace", "mace", true, false, false),
    VANILLA("Vanilla", "§6🌍 Vanilla", "vanilla", false, true, true),
    SMP("SMP", "§6⚔ SMP", "smp", false, true, true),
    DIASMP("DiaSMP", "§6💎 DiaSMP", "diasmp", false, true, true),
    SPEAR_MACE("Spear-Mace", "§6🗡 Spear-Mace", "spear-mace", true, false, false);

    private final String displayName;
    private final String coloredName;
    private final String configName;
    private final boolean isArenaRestricted;
    private final boolean canBreakBlocks;
    private final boolean canPlaceBlocks;

    DuelGameMode(String displayName, String coloredName, String configName,
                 boolean isArenaRestricted, boolean canBreakBlocks, boolean canPlaceBlocks) {
        this.displayName = displayName;
        this.coloredName = coloredName;
        this.configName = configName;
        this.isArenaRestricted = isArenaRestricted;
        this.canBreakBlocks = canBreakBlocks;
        this.canPlaceBlocks = canPlaceBlocks;
    }

    public String getDisplayName() { return displayName; }
    public String getColoredName() { return coloredName; }
    public String getConfigName() { return configName; }
    public boolean isArenaRestricted() { return isArenaRestricted; }
    public boolean canBreakBlocks() { return canBreakBlocks; }
    public boolean canPlaceBlocks() { return canPlaceBlocks; }

    public boolean isNaturalRegenDisabled() {
        return this == UHC;
    }

    public boolean isPotionBased() {
        return this == POT || this == NETHPOT;
    }

    public boolean hasNetheriteArmor() {
        return this == NETHPOT;
    }

    public boolean isDiaSMP() {
        return this == DIASMP;
    }

    public static DuelGameMode fromConfig(String config) {
        for (DuelGameMode mode : values()) {
            if (mode.configName.equalsIgnoreCase(config)) {
                return mode;
            }
        }
        return null;
    }

    public static DuelGameMode fromName(String name) {
        for (DuelGameMode mode : values()) {
            if (mode.displayName.equalsIgnoreCase(name) || mode.configName.equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return null;
    }
}
