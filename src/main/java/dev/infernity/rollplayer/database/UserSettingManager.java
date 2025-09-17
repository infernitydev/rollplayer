package dev.infernity.rollplayer.database;

import dev.infernity.rollplayer.database.objects.UserSettings;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.concurrent.atomic.AtomicReference;

public class UserSettingManager {
    protected EntityManagerFactory entityManagerFactory;

    public UserSettingManager() {
        try (EntityManagerFactory emf = Persistence.createEntityManagerFactory("Rollplayer")){
            entityManagerFactory = emf;
        }
    }

    UserSettings getOrCreate(long id){
        AtomicReference<UserSettings> ref = new AtomicReference<>();

        entityManagerFactory.runInTransaction(entityManager -> {
            var settings = entityManager.find(UserSettings.class, id);
            if (settings == null){
                settings = new UserSettings();
                settings.setId(id);
                entityManager.persist(settings);
            }
            ref.set(settings);
        });

        return ref.get();
    }
}
