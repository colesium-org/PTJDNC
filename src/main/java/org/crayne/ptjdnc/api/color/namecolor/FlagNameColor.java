package org.crayne.ptjdnc.api.color.namecolor;

import net.kyori.adventure.text.Component;
import org.crayne.ptjdnc.api.ColorLike;
import org.crayne.ptjdnc.api.color.NameColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FlagNameColor extends NameColor {

    public FlagNameColor(@NotNull final List<? extends ColorLike> colors) {
        super(colors);
    }

    @NotNull
    public Component colorize(@NotNull final String string) {
        if (colors().isEmpty() || string.isBlank()) return Component.text(string);
        final int length = string.length();
        final int colors = colors().size();

        Component result = Component.text("");
        final int individualStringLength = (length + (length % colors)) / colors;

        for (int i = 0; i < colors; i++) {
            final ColorLike color = colors().get(i);
            final int nextIndex = i + 1;
            final boolean lastPart = nextIndex == colors;

            final int partBegin = i * individualStringLength;
            if (partBegin >= length) break;

            final int partEnd = lastPart
                    ? length
                    : Math.min(length, nextIndex * individualStringLength);

            final String flagStringPart = string.substring(partBegin, partEnd);

            result = result.append(color.stylize(flagStringPart));
        }

        return result;
    }

    @NotNull
    public String toString() {
        return "FlagNameColor{" +
                "colors=" + colors() +
                '}';
    }
}
