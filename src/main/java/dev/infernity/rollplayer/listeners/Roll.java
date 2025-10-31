package dev.infernity.rollplayer.listeners;

import dev.infernity.rollplayer.Resources;
import dev.infernity.rollplayer.listeners.templates.SimpleCommandListener;
import dev.infernity.rollplayer.rollplayerlib3.Parser;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
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

    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onCommandRan(@NotNull SlashCommandInteractionEvent event) {
        String input = event.getOption("roll",
                () -> Resources.getInstance().getSettingsManager().getSettings(event.getUser().getIdLong()).getDefaultRoll(),
                OptionMapping::getAsString);

        if (isInteger(input)){ // If people are doing "roll 20", they probably expect "roll d20"
            input = "d" + input;
        }
        ArrayList<String> evaluations;
        ArrayList<String> expressions;
        List<ContainerChildComponent> output = new ArrayList<>();

        try {
            expressions = Parser.removeWhitespace(input);
            if (expressions.size() > 5) throw new IllegalArgumentException("Rollplayer cannot roll more than 5 expressions at once");
            evaluations = Parser.evaluate(input);
        } catch (Exception e) {
            var errcode = Resources.getInstance().tryLogException(e, TextDisplay.ofFormat("Roll string: `%s`", input));
            event.replyComponents(createContainer(
                    TextDisplay.of("**Rollplayer has run into an issue:**"),
                    TextDisplay.ofFormat("%s", e.toString()),
                    TextDisplay.ofFormat("\n-# If this issue is unexpected, please contact the developers in [the support server](https://discord.gg/TT3vyT3tAD) and give them the following error code: %s", errcode)
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

        float minLerpHue = 0, maxLerpHue = 120f/360, overMaxLerpHue = 300f/360;
        float minHue = 0, maxHue = 200f/360;
        float brightness = 70f/100, saturation = 1;

        Container outputContainer = createContainer(output);

        boolean colorViable = true;
        for(String exp : expressions)
            if(exp.contains("{")) {
                colorViable = false;
                break;
            }

        if (colorViable) {
            float hue;
            double valueSum = 0;
            double valueMax = Parser.evaluateMinMax(expressions, "max");
            double valueMin = Parser.evaluateMinMax(expressions, "min");

            for(String s : evaluations) {
                if(s.startsWith("r")) {
                    String[] doubles = s.substring(2).split(" ");
                    for(String d : doubles)
                        valueSum += Double.parseDouble(d);
                } else valueSum += Double.parseDouble(s);
            }

            if (valueSum >= valueMax) {
                if (valueSum >= 2*valueMax) hue = overMaxLerpHue;
                else {
                    //overmax lerp
                    // think of this as (valueSum - valueMax) / (2*valueMax - valueMax)
                    float lerp = (float) ((valueSum - valueMax) / valueMax);
                    hue = lerp * (overMaxLerpHue - maxLerpHue) + maxLerpHue;
                }
            }
            else if (valueSum <= valueMin) hue = minHue;
            else {
                //what's lerpma?
                float lerp = (float) ((valueSum - valueMin) / (valueMax - valueMin));
                hue = lerp * (maxLerpHue - minLerpHue) + minLerpHue;
            }

            outputContainer = outputContainer.withAccentColor(Color.getHSBColor(hue, saturation, brightness));
        }

        event.replyComponents(outputContainer).useComponentsV2().queue();
    }
}
