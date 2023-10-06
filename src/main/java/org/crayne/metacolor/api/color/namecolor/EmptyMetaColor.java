package org.crayne.metacolor.api.color.namecolor;

import net.kyori.adventure.text.Component;
import org.crayne.metacolor.api.color.MetaColor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class EmptyMetaColor extends MetaColor {

    public EmptyMetaColor() {
        super(Collections.emptyList());
    }

    @NotNull
    public Component colorize(@NotNull final String string) {
        return Component.text(string);
    }

    public byte nameColorCode() {
        return 0;
    }

    @NotNull
    public String toString() {
        return "EmptyMetaColor";
    }
}
