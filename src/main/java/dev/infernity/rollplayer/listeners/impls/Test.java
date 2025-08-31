package dev.infernity.rollplayer.listeners.impls;

import dev.infernity.rollplayer.listeners.SimpleCommandListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Test extends SimpleCommandListener {
    private static final Logger log = LoggerFactory.getLogger(Test.class);

    public Test() {
        super("test", "test desc");
    }

    public void onCommandRan(@NotNull SlashCommandInteractionEvent event) {
        event.replyComponents(Container.of(
                TextDisplay.of("Hello from Java 24 and Components v2!")
        )).useComponentsV2().queue();
    }
}
