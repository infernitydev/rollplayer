package dev.infernity.rollplayer.listeners;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.List;

public interface CommandDataCapable {
    List<SlashCommandData> getCommandData();
}
