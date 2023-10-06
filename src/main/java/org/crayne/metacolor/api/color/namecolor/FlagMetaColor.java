package org.crayne.metacolor.api.color.namecolor;

import net.kyori.adventure.text.Component;
import org.crayne.metacolor.api.color.MetaColor;
import org.crayne.metacolor.api.color.RGB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FlagMetaColor extends MetaColor {

    public FlagMetaColor(@NotNull final List<RGB> colors) {
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
            final RGB color = colors().get(i);
            final String flagStringPart = string.substring(i * individualStringLength, Math.min(length, (i + 1) * individualStringLength));
            result = result.append(color.colorize(flagStringPart));
        }

        return result;
    }

    public byte nameColorCode() {
        return 3;
    }

    @NotNull
    public String toString() {
        return "FlagMetaColor{" +
                "colors=" + colors() +
                '}';
    }
}
