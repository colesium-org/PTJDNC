package org.crayne.metacolor.api.color.namecolor;

import net.kyori.adventure.text.Component;
import org.crayne.metacolor.api.MetaColorLike;
import org.crayne.metacolor.api.color.MetaColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FlagMetaColor extends MetaColor {

    public FlagMetaColor(@NotNull final List<? extends MetaColorLike> colors) {
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
            final MetaColorLike color = colors().get(i);
            final String flagStringPart = string.substring(i * individualStringLength, i + 1 == colors ? length : Math.min(length, (i + 1) * individualStringLength));
            result = result.append(color.stylize(flagStringPart));
        }

        return result;
    }

    @NotNull
    public String toString() {
        return "FlagMetaColor{" +
                "colors=" + colors() +
                '}';
    }
}
