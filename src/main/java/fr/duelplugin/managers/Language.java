package fr.duelplugin.managers;

public enum Language {
    FR("fr_fr", "Français"),
    EN("en_us", "English"),
    DE("de_de", "Deutsch"),
    ES("es_es", "Español");

    private final String file;
    private final String displayName;

    Language(String file, String displayName) {
        this.file = file;
        this.displayName = displayName;
    }

    public String getFile() {
        return file;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Language fromString(String s) {
        if (s == null) return EN;
        try {
            return valueOf(s.toUpperCase());
        } catch (Exception e) {
            return EN;
        }
    }
}
