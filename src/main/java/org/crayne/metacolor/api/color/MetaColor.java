package org.crayne.metacolor.api.color;

import com.google.common.primitives.Bytes;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.tuple.Pair;
import org.crayne.metacolor.api.color.namecolor.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class MetaColor {

    @NotNull
    private final List<RGB> colors;

    private static final int MAX_COLORS = 255;

    public MetaColor(@NotNull final List<RGB> colors) {
        if (colors.size() > MAX_COLORS) throw new IllegalArgumentException("Invalid color list; Only up to " + MAX_COLORS + " colors can be used for a MetaColor.");
        this.colors = colors;
    }

    @NotNull
    public List<RGB> colors() {
        return colors;
    }

    @NotNull
    public abstract Component colorize(@NotNull final String string);

    public abstract byte nameColorCode();

    public byte @NotNull [] encode() {
        return Bytes.concat(new byte[] {nameColorCode(), (byte) colors().size()}, Bytes.concat(colors.stream().map(RGB::encode).toList().toArray(new byte[0][])));
    }

    @NotNull
    public static Optional<Pair<MetaColor, Integer>> decode(final byte @NotNull [] encoded, final int offset) {
        if (encoded.length < 2) return Optional.empty();
        final int nameColorCode = encoded[offset];
        final int colorsAmount = encoded[offset + 1];
        final int componentsAmount = colorsAmount * 3;
        final List<RGB> colorsDecoded = new ArrayList<>();

        for (int i = 0; i + 2 < componentsAmount; i += 3) {
            final int j = offset + 2 + i;
            final int r = Byte.toUnsignedInt(encoded[j]),
                    g = Byte.toUnsignedInt(encoded[j + 1]),
                    b = Byte.toUnsignedInt(encoded[j + 2]);

            final RGB rgb = RGB.of(r, g, b);
            colorsDecoded.add(rgb);
        }

        if (colorsDecoded.size() != colorsAmount) return Optional.empty();

        return Optional.of(Pair.of(switch (nameColorCode) {
            case 1 -> new SingletonMetaColor(colorsDecoded);
            case 2 -> new AlternatingMetaColor(colorsDecoded);
            case 3 -> new FlagMetaColor(colorsDecoded);
            case 4 -> new GradientMetaColor(colorsDecoded);
            default -> new EmptyMetaColor();
        }, offset + 2 + colorsDecoded.size() * 3));
    }

    @NotNull
    public String toString() {
        return "MetaColor{" +
                "colors=" + colors +
                '}';
    }
}
