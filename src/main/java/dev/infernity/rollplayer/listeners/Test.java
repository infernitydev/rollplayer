package dev.infernity.rollplayer.listeners;

import dev.infernity.rollplayer.listeners.templates.SimpleCommandListener;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class Test extends SimpleCommandListener {
    public Test() {
        super("test", "test desc", "\uD83D\uDD27");
    }

    public void onCommandRan(@NotNull SlashCommandInteractionEvent event) {
        event.replyComponents(createContainer(
                TextDisplay.of("I made a method to create command containers in SimpleCommandListener. Muahaha.")
        )).useComponentsV2().queue();
    }
}
