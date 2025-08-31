package dev.infernity.rollplayer.listeners;

import dev.infernity.rollplayer.listeners.templates.SimpleCommandListener;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class Test extends SimpleCommandListener {
    public Test() {
        super("test", "test desc");
    }

    public void onCommandRan(@NotNull SlashCommandInteractionEvent event) {
        event.replyComponents(Container.of(
                TextDisplay.of("Hello from Java 24 and Components v2!")
        )).useComponentsV2().queue();
    }
}
