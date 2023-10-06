package org.crayne.metacolor.api.color.namecolor;

import net.kyori.adventure.text.Component;
import org.crayne.metacolor.api.color.MetaColor;
import org.crayne.metacolor.api.color.RGB;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SingletonMetaColor extends MetaColor {

    public SingletonMetaColor(@NotNull final List<RGB> colors) {
        super(colors);
        if (colors.size() != 1) throw new IllegalArgumentException();
    }

    public SingletonMetaColor(@NotNull final RGB color) {
        super(Collections.singletonList(color));
    }

    @NotNull
    public Component colorize(@NotNull final String string) {
        return Component.text(string).color(color().textColor());
    }

    @NotNull
    public RGB color() {
        return colors().get(0);
    }

    public byte nameColorCode() {
        return 1;
    }

    @NotNull
    public String toString() {
        return "SingletonMetaColor{" +
                "color=" + color() +
                '}';
    }
}