package dev.infernity.rollplayer.listeners.interfaces;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;

public interface CommandDataCapable {
    List<CommandData> getCommandData();
}
