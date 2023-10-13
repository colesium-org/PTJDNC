package org.crayne.ptjdnc.api.color;

import com.google.common.primitives.Bytes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.tuple.Pair;
import org.crayne.ptjdnc.api.ColorLike;
import org.crayne.ptjdnc.api.color.namecolor.*;
import org.crayne.ptjdnc.api.style.NameDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class NameColor implements ColorLike {

    @NotNull
    private final List<? extends ColorLike> colors;

    private static final int MAX_COLORS = 255;

    public NameColor(@NotNull final List<? extends ColorLike> colors) {
        if (colors.size() > MAX_COLORS) throw new IllegalArgumentException("Invalid color list; Only up to " + MAX_COLORS + " colors can be used for a NameStyle.");
        this.colors = colors;
    }

    @NotNull
    public List<? extends ColorLike> colors() {
        return colors;
    }

    @NotNull
    public abstract Component colorize(@NotNull final String string);

    @NotNull
    public Component stylize(@NotNull final String s) {
        return colorize(s);
    }

    @NotNull
    public Component colorize(@NotNull final Component component) {
        return colorize(PlainTextComponentSerializer.plainText().serialize(component));
    }

    @NotNull
    public Component stylize(@NotNull final Component component) {
        return colorize(component);
    }

    public byte @NotNull [] encode() {
        return Bytes.concat(new byte[] {(byte) colors().size()}, Bytes.concat(colors.stream().map(ColorLike::encode).toList().toArray(new byte[0][])));
    }

    @NotNull
    public static Optional<Pair<NameColor, Integer>> decode(@NotNull final NameDecoration textDecoration, final byte @NotNull [] encoded, final int offset) {
        if (encoded.length < 2) return Optional.empty();
        final int colorsAmount = encoded[offset];
        final int componentsAmount = colorsAmount * 3;
        final List<RGB> colorsDecoded = new ArrayList<>();

        for (int i = 0; i + 2 < componentsAmount; i += 3) {
            final int j = offset + 1 + i;
            final int r = Byte.toUnsignedInt(encoded[j]),
                    g = Byte.toUnsignedInt(encoded[j + 1]),
                    b = Byte.toUnsignedInt(encoded[j + 2]);

            final RGB rgb = RGB.of(r, g, b);
            colorsDecoded.add(rgb);
        }

        if (colorsDecoded.size() != colorsAmount) return Optional.empty();
        final int newOffset = offset + 1 + colorsDecoded.size() * 3;

        if (textDecoration.gradient())    return Optional.of(Pair.of(new GradientNameColor(colorsDecoded), newOffset));
        if (textDecoration.alternating()) return Optional.of(Pair.of(new AlternatingNameColor(colorsDecoded), newOffset));
        if (textDecoration.flag())        return Optional.of(Pair.of(new FlagNameColor(colorsDecoded), newOffset));

        return Optional.of(Pair.of(new SingletonNameColor(colorsDecoded), newOffset));
    }

    @NotNull
    public String toString() {
        return "NameStyle{" +
                "colors=" + colors +
                '}';
    }
}
