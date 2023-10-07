package org.crayne.metacolor.api.style;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.crayne.metacolor.api.MetaColorLike;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MetaDecoration implements MetaColorLike {

    private final boolean bold, italic, strikethrough, underlined, obfuscated, gradient, flag, alternating;

    private MetaDecoration(final boolean bold, final boolean italic, final boolean strikethrough, final boolean underlined,
                           final boolean obfuscated, final boolean gradient, final boolean flag, final boolean alternating) {
        this.bold = bold;
        this.italic = italic;
        this.strikethrough = strikethrough;
        this.underlined = underlined;
        this.obfuscated = obfuscated;
        this.gradient = gradient;
        this.flag = flag;
        this.alternating = alternating;
    }

    @NotNull
    public Component decorate(@NotNull final Component component) {
        return component.decorate(decorations());
    }

    @NotNull
    public Component stylize(@NotNull final Component component) {
        return decorate(component);
    }

    @NotNull
    public Component decorate(@NotNull final String s) {
        return decorate(Component.text(s));
    }

    @NotNull
    public Component stylize(@NotNull final String s) {
        return decorate(s);
    }

    @NotNull
    public TextDecoration[] decorations() {
        return Arrays.stream(TextDecoration.values())
                .filter(this::hasDecoration)
                .toList()
                .toArray(new TextDecoration[0]);
    }

    @NotNull
    public static MetaDecoration combine(@NotNull final Collection<MetaDecoration> decorations) {
        final Builder builder = new Builder();
        decorations.forEach(builder::copyAllTrueValues);
        return builder.create();
    }

    @NotNull
    public List<String> colors() {
        final List<String> colors = new ArrayList<>();
        if (bold) colors.add("bold");
        if (italic) colors.add("italic");
        if (strikethrough) colors.add("strikethrough");
        if (underlined) colors.add("underlined");
        if (obfuscated) colors.add("obfuscated");
        if (gradient) colors.add("gradient");
        if (flag) colors.add("flag");
        if (alternating) colors.add("alternating");

        return colors;
    }

    @NotNull
    public static MetaDecoration none() {
        return new MetaDecoration.Builder().create();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasSpecialDecorators() {
        return gradient || flag || alternating;
    }

    public boolean hasUncombinableGradient() {
        return gradient && (flag || alternating);
    }

    public boolean hasUncombinableFlag() {
        return flag && (gradient || alternating);
    }

    public boolean hasUncombinableAlternating() {
        return alternating && (flag || gradient);
    }

    public boolean hasInvalidCombinations() {
        return hasUncombinableGradient() || hasUncombinableFlag() || hasUncombinableAlternating();
    }

    public byte @NotNull [] encode() {
        return new byte[] {(byte) ((bold ? 1 : 0)
                | ((italic        ? 1 : 0) << 1)
                | ((strikethrough ? 1 : 0) << 2)
                | ((underlined    ? 1 : 0) << 3)
                | ((obfuscated    ? 1 : 0) << 4)
                | ((gradient      ? 1 : 0) << 5)
                | ((flag          ? 1 : 0) << 6)
                | ((alternating   ? 1 : 0) << 7)
        )};
    }

    @NotNull
    public static Optional<MetaDecoration> decode(final byte encoded) {
        final boolean bold           = (encoded & 1) != 0;
        final boolean italic         = (encoded & (1 << 1)) != 0;
        final boolean strikethrough  = (encoded & (1 << 2)) != 0;
        final boolean underlined     = (encoded & (1 << 3)) != 0;
        final boolean obfuscated     = (encoded & (1 << 4)) != 0;
        final boolean gradient       = (encoded & (1 << 5)) != 0;
        final boolean flag           = (encoded & (1 << 6)) != 0;
        final boolean alternating    = (encoded & (1 << 7)) != 0;
        return Optional.of(new MetaDecoration(bold, italic, strikethrough, underlined, obfuscated, gradient, flag, alternating));
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

    public boolean flag() {
        return flag;
    }

    public boolean alternating() {
        return alternating;
    }

    public boolean gradient() {
        return gradient;
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

        private boolean bold, italic, strikethrough, underlined, obfuscated, gradient, flag, alternating;

        public Builder() {}

        public Builder(@NotNull final MetaDecoration decoration) {
            this.bold = decoration.bold;
            this.italic = decoration.italic;
            this.strikethrough = decoration.strikethrough;
            this.underlined = decoration.underlined;
            this.obfuscated = decoration.obfuscated;
            this.gradient = decoration.gradient;
            this.flag = decoration.flag;
            this.alternating = decoration.alternating;
        }

        @NotNull
        public MetaDecoration create() {
            return new MetaDecoration(bold, italic, strikethrough, underlined, obfuscated, gradient, flag, alternating);
        }

        public void copyAllTrueValues(@NotNull final MetaDecoration decoration) {
            if (decoration.bold)          bold = true;
            if (decoration.italic)        italic = true;
            if (decoration.strikethrough) strikethrough = true;
            if (decoration.underlined)    underlined = true;
            if (decoration.obfuscated)    obfuscated = true;
            if (decoration.gradient)      gradient = true;
            if (decoration.flag)          flag = true;
            if (decoration.alternating)   alternating = true;
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

        public boolean gradient() {
            return gradient;
        }

        public boolean flag() {
            return flag;
        }

        public boolean alternating() {
            return alternating;
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
        public Builder gradient(final boolean gradient) {
            this.gradient = gradient;
            return this;
        }

        @NotNull
        public Builder flag(final boolean flag) {
            this.flag = flag;
            return this;
        }

        @NotNull
        public Builder alternating(final boolean alternating) {
            this.alternating = alternating;
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
                    ", gradient=" + gradient +
                    ", flag=" + flag +
                    ", alternating=" + alternating +
                    '}';
        }

    }

    public boolean undecorated() {
        return equals(none());
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final MetaDecoration that = (MetaDecoration) o;

        if (bold != that.bold) return false;
        if (italic != that.italic) return false;
        if (strikethrough != that.strikethrough) return false;
        if (underlined != that.underlined) return false;
        if (obfuscated != that.obfuscated) return false;
        if (gradient != that.gradient) return false;
        if (flag != that.flag) return false;
        return alternating == that.alternating;
    }

    @NotNull
    public String toString() {
        return "Builder{" +
                "bold=" + bold +
                ", italic=" + italic +
                ", strikethrough=" + strikethrough +
                ", underlined=" + underlined +
                ", obfuscated=" + obfuscated +
                ", gradient=" + gradient +
                ", flag=" + flag +
                ", alternating=" + alternating +
                '}';
    }

}
