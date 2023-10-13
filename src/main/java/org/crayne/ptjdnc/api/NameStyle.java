package org.crayne.ptjdnc.api;

import com.google.common.primitives.Bytes;
import net.kyori.adventure.text.Component;
import org.crayne.ptjdnc.api.color.NameColor;
import org.crayne.ptjdnc.api.style.NameDecoration;
import org.jetbrains.annotations.NotNull;

public class NameStyle implements ColorLike {

    @NotNull
    private final NameColor color;

    @NotNull
    private final NameDecoration decoration;

    public NameStyle(@NotNull final NameColor color, @NotNull final NameDecoration decoration) {
        this.color = color;
        this.decoration = decoration;
    }

    @NotNull
    public NameColor color() {
        return color;
    }

    @NotNull
    public NameDecoration decoration() {
        return decoration;
    }

    @NotNull
    public Component stylize(@NotNull final Component component) {
        return decoration.stylize(color.stylize(component));
    }

    @NotNull
    public Component stylize(@NotNull final String s) {
        return stylize(Component.text(s));
    }

    public byte @NotNull [] encode() {
        return Bytes.concat(decoration.encode(), color.encode());
    }

    public String toString() {
        return "NameStyle{" +
                "color=" + color +
                ", decoration=" + decoration +
                '}';
    }
}
