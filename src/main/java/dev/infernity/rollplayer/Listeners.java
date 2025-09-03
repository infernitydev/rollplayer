package dev.infernity.rollplayer;

import dev.infernity.rollplayer.listeners.Choose;
import dev.infernity.rollplayer.listeners.EightBall;
import dev.infernity.rollplayer.listeners.TestArguments;
import dev.infernity.rollplayer.listeners.interfaces.CommandDataCapable;
import dev.infernity.rollplayer.listeners.Test;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.ArrayList;
import java.util.List;

public class Listeners {
    public List<EventListener> listeners;
    public ArrayList<CommandData> commands = new ArrayList<>();

    public Listeners(){
        this.listeners = List.of(new Test(), new TestArguments(), new EightBall(), new Choose());
        for (EventListener listener : listeners) {
            if (listener instanceof CommandDataCapable capable) {
                commands.addAll(capable.getCommandData());
            }
        }
    }
}
