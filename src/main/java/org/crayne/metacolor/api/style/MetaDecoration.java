package org.crayne.metacolor.api.style;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public class MetaDecoration {

    private final boolean bold, italic, strikethrough, underlined, obfuscated;

    private MetaDecoration(final boolean bold, final boolean italic, final boolean strikethrough, final boolean underlined, final boolean obfuscated) {
        this.bold = bold;
        this.italic = italic;
        this.strikethrough = strikethrough;
        this.underlined = underlined;
        this.obfuscated = obfuscated;
    }

    @NotNull
    public Component decorate(@NotNull final Component component) {
        return component.decorate(decorations());
    }

    @NotNull
    public TextDecoration[] decorations() {
        return Arrays.stream(TextDecoration.values())
                .filter(this::hasDecoration)
                .toList()
                .toArray(new TextDecoration[0]);
    }

    public byte encode() {
        return (byte) ((bold ? 1 : 0)
                        | ((italic        ? 1 : 0) << 1)
                        | ((strikethrough ? 1 : 0) << 2)
                        | ((underlined    ? 1 : 0) << 3)
                        | ((obfuscated    ? 1 : 0) << 4));
    }

    @NotNull
    public static Optional<MetaDecoration> decode(final byte encoded) {
        final boolean bold          = (encoded & 1) != 0;
        final boolean italic        = (encoded & (1 << 1)) != 0;
        final boolean strikethrough = (encoded & (1 << 2)) != 0;
        final boolean underlined    = (encoded & (1 << 3)) != 0;
        final boolean obfuscated    = (encoded & (1 << 4)) != 0;
        return Optional.of(new MetaDecoration(bold, italic, strikethrough, underlined, obfuscated));
    }

    public boolean hasDecoration(@NotNull final TextDecoration decoration) {
        return switch (decoration) {
            case BOLD -> bold;
            case ITALIC -> italic;
            case STRIKETHROUGH -> strikethrough;
            case UNDERLINED -> underlined;
            case OBFUSCATED -> obfuscated;
        };
    }

    public boolean bold() {
        return bold;
    }

    public boolean italic() {
        return italic;
    }

    public boolean strikethrough() {
        return strikethrough;
    }

    public boolean underlined() {
        return underlined;
    }

    public boolean obfuscated() {
        return obfuscated;
    }

    public static class Builder {

        private boolean bold, italic, strikethrough, underlined, obfuscated;

        public Builder() {}

        @NotNull
        public MetaDecoration create() {
            return new MetaDecoration(bold, italic, strikethrough, underlined, obfuscated);
        }

        public boolean bold() {
            return bold;
        }

        public boolean italic() {
            return italic;
        }

        public boolean strikethrough() {
            return strikethrough;
        }

        public boolean underlined() {
            return underlined;
        }

        public boolean obfuscated() {
            return obfuscated;
        }

        @NotNull
        public Builder bold(final boolean bold) {
            this.bold = bold;
            return this;
        }

        @NotNull
        public Builder italic(final boolean italic) {
            this.italic = italic;
            return this;
        }

        @NotNull
        public Builder strikethrough(final boolean strikethrough) {
            this.strikethrough = strikethrough;
            return this;
        }

        @NotNull
        public Builder underlined(final boolean underline) {
            this.underlined = underline;
            return this;
        }

        @NotNull
        public Builder obfuscated(final boolean obfuscated) {
            this.obfuscated = obfuscated;
            return this;
        }

        @NotNull
        public String toString() {
            return "Builder{" +
                    "bold=" + bold +
                    ", italic=" + italic +
                    ", strikethrough=" + strikethrough +
                    ", underlined=" + underlined +
                    ", obfuscated=" + obfuscated +
                    '}';
        }
    }

    @NotNull
    public String toString() {
        return "MetaDecoration{" +
                "bold=" + bold +
                ", italic=" + italic +
                ", strikethrough=" + strikethrough +
                ", underlined=" + underlined +
                ", obfuscated=" + obfuscated +
                '}';
    }
}
