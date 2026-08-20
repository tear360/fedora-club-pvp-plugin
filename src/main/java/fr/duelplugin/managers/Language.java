package fr.duelplugin.managers;

public enum Language {
    FR("Français"),
    EN("English");

    private final String displayName;

    Language(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Language fromString(String s) {
        try {
            return valueOf(s.toUpperCase());
        } catch (Exception e) {
            return FR;
        }
    }
}
