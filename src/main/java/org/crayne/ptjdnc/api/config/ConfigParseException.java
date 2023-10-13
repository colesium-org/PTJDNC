package org.crayne.ptjdnc.api.config;

import org.jetbrains.annotations.NotNull;

public class ConfigParseException extends RuntimeException {

    public ConfigParseException(@NotNull final Throwable t) {
        super(t);
    }

    public ConfigParseException(@NotNull final String s) {
        super(s);
    }

    public ConfigParseException(@NotNull final Throwable t, @NotNull final String s) {
        super(s, t);
    }

}
