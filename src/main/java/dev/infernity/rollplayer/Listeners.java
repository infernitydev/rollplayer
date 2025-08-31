package dev.infernity.rollplayer;

import dev.infernity.rollplayer.listeners.CommandDataCapable;
import dev.infernity.rollplayer.listeners.impls.Test;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.List;

public class Listeners {
    public List<EventListener> listeners;
    public ArrayList<SlashCommandData> commands = new ArrayList<>();

    public Listeners(){
        this.listeners = List.of(new Test());
        for (EventListener listener : listeners) {
            if (listener instanceof CommandDataCapable capable) {
                commands.addAll(capable.getCommandData());
            }
        }
    }
}
