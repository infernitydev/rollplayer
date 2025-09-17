package dev.infernity.rollplayer.database.objects;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "user_settings")
public class UserSettings {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "default_roll")
    @ColumnDefault("'d20'")
    private String defaultRoll;

    public Integer getVersion() {
        return version;
    }

    public String getDefaultRoll() {
        return defaultRoll;
    }

    public void setDefaultRoll(String defaultRoll) {
        this.defaultRoll = defaultRoll;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}