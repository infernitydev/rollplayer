package dev.infernity.rollplayer.listeners.templates;

import dev.infernity.rollplayer.listeners.interfaces.CommandDataCapable;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

///
/// A simple listener for slash commands.
/// Override onCommandRan for when the command is ran.
/// Override getCommandData to change the command details. It should only return one command which follows the command name.
/// Create buttons whose IDs start with the command name then a colon to
public abstract class SimpleCommandListener implements EventListener, CommandDataCapable {
    protected final String commandName;
    protected final String commandDescription;

    public SimpleCommandListener(String commandName, String commandDescription) {
        this.commandName = commandName;
        this.commandDescription = commandDescription;
    }

    public List<SlashCommandData> getCommandData(){
        return List.of(Commands.slash(commandName, commandDescription));
    }

    public abstract void onCommandRan(@NotNull SlashCommandInteractionEvent event);

    public void onButtonPress(@NotNull ButtonInteractionEvent event) {
        throw new RuntimeException("A button was pressed with this listener, but no override was given to onButtonPress! Event ID: " + event.getComponentId());
    }

    @Override
    public void onEvent(@NotNull GenericEvent genericEvent) {
        if (genericEvent instanceof SlashCommandInteractionEvent event) {
            if (event.getName().equals(commandName)) {
                onCommandRan(event);
            }
        } else if (genericEvent instanceof ButtonInteractionEvent event) {
            String[] id = event.getComponentId().split(":");
            if (Objects.equals(id[0], commandName)) {
                onButtonPress(event);
            }
        }
    }
}
