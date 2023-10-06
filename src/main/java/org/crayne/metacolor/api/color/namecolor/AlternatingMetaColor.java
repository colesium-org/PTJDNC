package org.crayne.metacolor.api.color.namecolor;

import net.kyori.adventure.text.Component;
import org.crayne.metacolor.api.MetaColorLike;
import org.crayne.metacolor.api.color.MetaColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AlternatingMetaColor extends MetaColor {

    public AlternatingMetaColor(@NotNull final List<? extends MetaColorLike> colors) {
        super(colors);
    }

    @NotNull
    public Component colorize(@NotNull final String string) {
        if (colors().isEmpty() || string.isBlank()) return Component.text(string);
        Component result = Component.text("");

        for (int i = 0; i < string.length(); i++) {
            final char character = string.charAt(i);
            final MetaColorLike color = colors().get(i % colors().size());
            result = result.append(color.stylize(character));
        }
        return result;
    }

    @NotNull
    public String toString() {
        return "AlternatingMetaColor{" +
                "colors=" + colors() +
                '}';
    }

}
