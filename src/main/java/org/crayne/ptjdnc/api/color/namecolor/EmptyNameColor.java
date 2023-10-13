package org.crayne.ptjdnc.api.color.namecolor;

import net.kyori.adventure.text.Component;
import org.crayne.ptjdnc.api.color.NameColor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class EmptyNameColor extends NameColor {

    public EmptyNameColor() {
        super(Collections.emptyList());
    }

    @NotNull
    public Component colorize(@NotNull final String string) {
        return Component.text(string);
    }

    @NotNull
    public String toString() {
        return "EmptyNameColor";
    }
}
