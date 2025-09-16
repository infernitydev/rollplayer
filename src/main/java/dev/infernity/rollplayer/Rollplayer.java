package dev.infernity.rollplayer;

import dev.infernity.rollplayer.files.JarPather;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Rollplayer extends ListenerAdapter {
    public static void main(String[] args) throws ConfigurationException {
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
        Objects.requireNonNull(api.getGuildById(1223799616915636287L)).updateCommands().addCommands(listeners.commands.toArray(new CommandData[0])).queue();
        super.onReady(event);
        Resources.INSTANCE.getLogger().info("Readied up!");
    }
}
