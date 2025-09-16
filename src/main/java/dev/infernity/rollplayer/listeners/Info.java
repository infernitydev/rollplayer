package dev.infernity.rollplayer.listeners;

import dev.infernity.rollplayer.Resources;
import dev.infernity.rollplayer.listeners.templates.SimpleCommandListener;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class Info extends SimpleCommandListener {
    public Info() {
        super("info", "info desc", "<:rollplayericon:1417614051290255412>");
    }

    public void onCommandRan(@NotNull SlashCommandInteractionEvent event) {
        event.replyComponents(createContainer(
                TextDisplay.ofFormat("### %s %s built at %s", Resources.INSTANCE.getName(), Resources.INSTANCE.getVersion(), Resources.INSTANCE.getTimestamp()),
                TextDisplay.ofFormat("Java %d", Runtime.version().feature()),
                TextDisplay.ofFormat("JDA %s", JDAInfo.VERSION)
        )).useComponentsV2().queue();
    }
}
