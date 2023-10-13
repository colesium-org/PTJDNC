package org.crayne.ptjdnc.api.color.namecolor;

import net.kyori.adventure.text.Component;
import org.crayne.ptjdnc.api.ColorLike;
import org.crayne.ptjdnc.api.color.NameColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AlternatingNameColor extends NameColor {

    public AlternatingNameColor(@NotNull final List<? extends ColorLike> colors) {
        super(colors);
    }

    @NotNull
    public Component colorize(@NotNull final String string) {
        if (colors().isEmpty() || string.isBlank()) return Component.text(string);
        Component result = Component.text("");

        for (int i = 0; i < string.length(); i++) {
            final char character = string.charAt(i);
            final ColorLike color = colors().get(i % colors().size());
            result = result.append(color.stylize(character));
        }
        return result;
    }

    @NotNull
    public String toString() {
        return "AlternatingNameColor{" +
                "colors=" + colors() +
                '}';
    }

}
