package dev.infernity.rollplayer;

import dev.infernity.rollplayer.listeners.*;
import dev.infernity.rollplayer.listeners.interfaces.CommandDataCapable;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.ArrayList;
import java.util.List;

public class Listeners {
    public List<EventListener> listeners;
    public ArrayList<CommandData> commands = new ArrayList<>();

    public Listeners(){
        this.listeners = List.of(new EightBall(), new Choose(), new TicTacToe(), new Info(), new Roll(), new Settings());
        // this.listeners.addAll(List.of(new Test(), new TestArguments()));
        for (EventListener listener : listeners) {
            if (listener instanceof CommandDataCapable capable) {
                commands.addAll(capable.getCommandData());
            }
        }
    }
}
