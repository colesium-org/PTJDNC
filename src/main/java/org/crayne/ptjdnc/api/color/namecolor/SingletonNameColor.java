package org.crayne.ptjdnc.api.color.namecolor;

import net.kyori.adventure.text.Component;
import org.crayne.ptjdnc.api.ColorLike;
import org.crayne.ptjdnc.api.color.NameColor;
import org.crayne.ptjdnc.api.color.RGB;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SingletonNameColor extends NameColor {

    public SingletonNameColor(@NotNull final List<? extends ColorLike> colors) {
        super(colors);
        if (colors.size() != 1) throw new IllegalArgumentException();
    }

    public SingletonNameColor(@NotNull final RGB color) {
        super(Collections.singletonList(color));
    }

    @NotNull
    public Component colorize(@NotNull final String string) {
        return color().stylize(string);
    }

    @NotNull
    public ColorLike color() {
        return colors().get(0);
    }

    @NotNull
    public String toString() {
        return "SingletonNameColor{" +
                "color=" + color() +
                '}';
    }
}
