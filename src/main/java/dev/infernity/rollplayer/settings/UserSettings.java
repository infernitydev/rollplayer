package dev.infernity.rollplayer.settings;

public class UserSettings {
    private static final int CURRENT_VERSION = 1;

    private final long userId;
    private int version;
    private String defaultRoll;

    public UserSettings(long userId) {
        this.userId = userId;
        this.version = CURRENT_VERSION;
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

    @SuppressWarnings("ConstantValue") // Can depend on the data so it's not actually constant as IntelliJ thinks it is
    public void performMigration() {
        if (this.version < CURRENT_VERSION) {
            if (this.version < 1) {
                // Migrations for settings created before version 1
                if (this.defaultRoll == null) {
                    this.defaultRoll = "1d20";
                }
            }

            this.version = CURRENT_VERSION;
        }
    }
}