package org.crayne.metacolor.api.color;

import com.google.common.primitives.Bytes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.tuple.Pair;
import org.crayne.metacolor.api.MetaColorLike;
import org.crayne.metacolor.api.color.namecolor.*;
import org.crayne.metacolor.api.style.MetaDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class MetaColor implements MetaColorLike {

    @NotNull
    private final List<? extends MetaColorLike> colors;

    private static final int MAX_COLORS = 255;

    public MetaColor(@NotNull final List<? extends MetaColorLike> colors) {
        if (colors.size() > MAX_COLORS) throw new IllegalArgumentException("Invalid color list; Only up to " + MAX_COLORS + " colors can be used for a MetaColor.");
        this.colors = colors;
    }

    @NotNull
    public List<? extends MetaColorLike> colors() {
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
        return Bytes.concat(new byte[] {(byte) colors().size()}, Bytes.concat(colors.stream().map(MetaColorLike::encode).toList().toArray(new byte[0][])));
    }

    @NotNull
    public static Optional<Pair<MetaColor, Integer>> decode(@NotNull final MetaDecoration textDecoration, final byte @NotNull [] encoded, final int offset) {
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

        if (textDecoration.gradient())    return Optional.of(Pair.of(new GradientMetaColor(colorsDecoded), newOffset));
        if (textDecoration.alternating()) return Optional.of(Pair.of(new AlternatingMetaColor(colorsDecoded), newOffset));
        if (textDecoration.flag())        return Optional.of(Pair.of(new FlagMetaColor(colorsDecoded), newOffset));

        return Optional.of(Pair.of(new SingletonMetaColor(colorsDecoded), newOffset));
    }

    @NotNull
    public String toString() {
        return "MetaColor{" +
                "colors=" + colors +
                '}';
    }
}
