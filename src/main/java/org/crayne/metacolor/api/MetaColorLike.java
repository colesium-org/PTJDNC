package org.crayne.metacolor.api;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface MetaColorLike {

    @NotNull
    Component stylize(@NotNull final Component component);

    @NotNull
    Component stylize(@NotNull final String s);

    @NotNull
    default Component stylize(final char character) {
        return stylize(Component.text(character));
    }

    byte @NotNull [] encode();
}
