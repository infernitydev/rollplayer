package dev.infernity.rollplayer.settings;

public class UserSettings {
    private final long userId;
    private String defaultRoll;

    public UserSettings(long userId) {
        this.userId = userId;
        this.defaultRoll = "1d20";
    }

    @SuppressWarnings("unused") // Here for consistency.
    public long getUserId() {
        return userId;
    }

    public String getDefaultRoll() {
        return defaultRoll;
    }

    public void setDefaultRoll(String defaultRoll) {
        this.defaultRoll = defaultRoll;
    }
}
