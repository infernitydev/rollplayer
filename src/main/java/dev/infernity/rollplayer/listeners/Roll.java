package dev.infernity.rollplayer.listeners;

import dev.infernity.rollplayer.Resources;
import dev.infernity.rollplayer.listeners.templates.SimpleCommandListener;
import dev.infernity.rollplayer.rollplayerlib3.Parser;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Roll extends SimpleCommandListener {
    public Roll(){
        super("roll", "They see me rollin', they hatin'", "\uD83C\uDFB2");
    }

    @Override
    public List<CommandData> getCommandData(){
        return List.of(
                Commands.slash(commandName, commandDescription)
                        .setContexts(InteractionContextType.ALL)
                        .addOption(OptionType.STRING, "roll", "Roll expressions, rules are explained in rollhelp", false)
        );
    }

    @Override
    public void onCommandRan(@NotNull SlashCommandInteractionEvent event) {
        String input = event.getOption("roll",
                () -> Resources.INSTANCE.getSettingsManager().getSettings(event.getUser().getIdLong()).getDefaultRoll(),
                OptionMapping::getAsString);
        ArrayList<String> evaluations;
        ArrayList<String> expressions;
        List<ContainerChildComponent> output = new ArrayList<>();

        try {
            evaluations = Parser.evaluate(input);
            expressions = Parser.removeWhitespace(input);
        } catch (IllegalArgumentException e) {
            event.replyComponents(createContainer(
                TextDisplay.of("**Rollplayer has run into an issue:**"),
                TextDisplay.ofFormat("%s", e.toString()),
                TextDisplay.of("\n-# If this issue is unexpected, please contact the developers immediately")
            )).useComponentsV2().queue();
            return;
        }

        output.add(TextDisplay.ofFormat("### --- %s ---", input));
        // add each expression-evaluation pair
        for (int exp = 0; exp < evaluations.size(); exp++) {
            if (expressions.size() > 1) {
                output.add(TextDisplay.ofFormat("**%s**", expressions.get(exp)));
            }

            String[] values;
            if (evaluations.get(exp).startsWith("r")) { // roll list clause
                values = evaluations.get(exp).substring(2).split(" ");
            } else { // single output clause
                values = new String[]{evaluations.get(exp)};
            }

            // remove trailing zeroes
            for(int s = 0; s < values.length; s++)
                if(values[s].endsWith(".0"))
                    values[s] =  values[s].substring(0, values[s].length()-2);

            StringBuilder line = new StringBuilder("[");
            for(int s = 0; s < values.length; s++) {
                line.append(values[s]);
                if(s < values.length-1)
                    line.append(", ");
            }
            if(values.length > 1){ //   append the total
                double total = 0;
                for(String s : values)
                    total += Double.parseDouble(s);

                if(total == (int)total) //  remove trailing zero
                    line.append(" (total: ").append((int)total).append(")");
                else
                    line.append(" (total: ").append(total).append(")");
            }
            line.append("]");
            output.add(TextDisplay.ofFormat("%s", line.toString()));
        }

        event.replyComponents(createContainer(output)).useComponentsV2().queue();
    }
}
