package org.crayne.metacolor.api.parse;

import org.jetbrains.annotations.NotNull;

public class MetaParseException extends RuntimeException {

    public MetaParseException(@NotNull final Throwable t) {
        super(t);
    }

    public MetaParseException(@NotNull final String s) {
        super(s);
    }

    public MetaParseException(@NotNull final Throwable t, @NotNull final String s) {
        super(s, t);
    }

}
