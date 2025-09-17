package dev.infernity.rollplayer.database.objects;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "user_settings")
public class UserSettings {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    
    @SuppressWarnings("unused") // Hibernate 7 recommends a version field for commonly changed fields, as settings will be.
    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "default_roll")
    @ColumnDefault("'d20'")
    private String defaultRoll;

    @SuppressWarnings("unused") // Hibernate 7 recommends a version field for commonly changed fields, as settings will be.
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