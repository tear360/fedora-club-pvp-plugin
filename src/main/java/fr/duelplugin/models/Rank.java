package fr.duelplugin.models;

public enum Rank {

    ADMIN("admin", "§4", "§4ADMIN"),
    MANAGER("manager", "§b", "§bMANAGER"),
    MODERATEUR("moderateur", "§e", "§eMODERATEUR"),
    VIP("vip", "§d", "§dVIP");

    private final String id;
    private final String color;
    private final String tag;

    Rank(String id, String color, String tag) {
        this.id = id;
        this.color = color;
        this.tag = tag;
    }

    public String getId() {
        return id;
    }

    public String getColor() {
        return color;
    }

    public String getTag() {
        return tag;
    }

    public String getDisplayName() {
        return name();
    }

    public static Rank fromId(String input) {
        if (input == null) return null;
        for (Rank rank : values()) {
            if (rank.id.equalsIgnoreCase(input) || rank.name().equalsIgnoreCase(input)) {
                return rank;
            }
        }
        return null;
    }
}