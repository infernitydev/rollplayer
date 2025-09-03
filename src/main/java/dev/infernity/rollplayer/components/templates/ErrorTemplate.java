package dev.infernity.rollplayer.components.templates;

import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;

import java.awt.*;

public class ErrorTemplate {
    public static Container of(String details) {
        return of(null, details);
    }

    public static Container of(String error, String details){
        if (error == null) {error = "An error occured!";}
        if (details == null) {
            return Container.of(TextDisplay.of("### " + error)).withAccentColor(Color.RED);
        }
        return Container.of(
                TextDisplay.of("### " + error),
                Separator.createDivider(Separator.Spacing.SMALL),
                TextDisplay.of(details)
        ).withAccentColor(Color.RED);
    }
}
