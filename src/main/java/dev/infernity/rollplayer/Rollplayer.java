package dev.infernity.rollplayer;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Rollplayer extends ListenerAdapter {
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(Resources.INSTANCE::saveSettings));

        String token = Resources.INSTANCE.getConfig().getString("discord.token");
        JDABuilder.createDefault(token).addEventListeners(new Rollplayer()).build();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        var api = event.getJDA();
        Resources.INSTANCE.getLogger().info("{} {} is initializing!", Resources.INSTANCE.getName(), Resources.INSTANCE.getVersion());
        var listeners = new Listeners();
        Resources.INSTANCE.getLogger().info("Loading {} listeners.", listeners.listeners.size());
        api.addEventListener(listeners.listeners.toArray());
        var debugServer = Resources.INSTANCE.getConfig().getLong("debug.testingServer", 0L);
        if (debugServer == 0L) {
            api.updateCommands().addCommands(listeners.commands.toArray(new CommandData[0]))
                    .onSuccess(_ -> Resources.INSTANCE.getLogger().info("Commands initialized globally."))
                    .queue();
        } else {
            Objects.requireNonNull(api.getGuildById(debugServer)).updateCommands().addCommands(listeners.commands.toArray(new CommandData[0]))
                    .onSuccess(_ -> Resources.INSTANCE.getLogger().info("Commands initialized to server {}.", debugServer))
                    .queue();
        }
        super.onReady(event);
        Resources.INSTANCE.getLogger().info("Readied up!");
    }
}
