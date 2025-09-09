package dev.infernity.rollplayer.rollplayerlib3.parser;

import org.rekex.annomacro.AnnoMacro;
import org.rekex.helper.anno.StrWs;

public @interface Str {
    final String whitespaceCharacters = " \t";

    String[] value();
    AnnoMacro<Str, StrWs> toStrWs = StrWs.Macro.of(Str::value, whitespaceCharacters);
}
