package org.crayne.metacolor.api;

import com.google.common.primitives.Bytes;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.crayne.metacolor.api.color.MetaColor;
import org.crayne.metacolor.api.style.MetaDecoration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.*;

public class MetaNameColor implements MetaColorLike {

    @NotNull
    private final MetaColor color;

    @NotNull
    private final MetaDecoration decoration;

    public MetaNameColor(@NotNull final MetaColor color, @NotNull final MetaDecoration decoration) {
        this.color = color;
        this.decoration = decoration;
    }

    @NotNull
    public MetaColor color() {
        return color;
    }

    @NotNull
    public MetaDecoration decoration() {
        return decoration;
    }

    @NotNull
    public Component stylize(@NotNull final Component component) {
        return decoration.stylize(color.stylize(component));
    }

    @NotNull
    public Component stylize(@NotNull final String s) {
        return stylize(Component.text(s));
    }

    public byte @NotNull [] encode() {
        return Bytes.concat(decoration.encode(), color.encode());
    }

    @NotNull
    public static Optional<Pair<MetaNameColor, Integer>> decode(final byte @NotNull [] encoded, final int offset) {
        if (encoded.length == 0) return Optional.empty();
        final byte decorationStringEncoded = encoded[offset];

        final Optional<MetaDecoration> decoration = MetaDecoration.decode(decorationStringEncoded);
        if (decoration.isEmpty()) return Optional.empty();

        final Optional<Pair<MetaColor, Integer>> color = MetaColor.decode(decoration.get(), encoded, offset + 1);
        if (color.isEmpty()) return Optional.empty();

        return Optional.of(Pair.of(new MetaNameColor(color.get().getKey(), decoration.get()), color.get().getValue()));
    }

    public byte @NotNull [] encodeWithUUID(@NotNull final UUID uuid) {
        return Bytes.concat(uuidToBytes(uuid), encode());
    }

    @NotNull
    public static Optional<Triple<MetaNameColor, UUID, Integer>> decodeWithUUID(final byte @NotNull [] encoded, final int offset) {
        if (encoded.length < 16) return Optional.empty();
        final byte[] uuidBytes = Arrays.copyOfRange(encoded, offset, offset + 16);
        final UUID uuid = bytesToUUID(uuidBytes);

        return decode(encoded, offset + 16).map(nameColor -> Triple.of(nameColor.getKey(), uuid, nameColor.getValue()));
    }

    public static void save(@NotNull final Map<UUID, MetaNameColor> nameColorMap, @NotNull final File file) throws IOException {
        Files.write(file.toPath(), Bytes.concat(nameColorMap.entrySet().stream().map(e -> e.getValue().encodeWithUUID(e.getKey())).toList().toArray(new byte[0][])));
    }

    @NotNull
    public static Map<UUID, MetaNameColor> load(@NotNull final File file) throws IOException {
        final Map<UUID, MetaNameColor> nameColorMap = new HashMap<>();

        final byte[] content = Files.readAllBytes(file.toPath());
        int offset = 0;
        while (content.length != offset) {
            final Triple<MetaNameColor, UUID, Integer> triple = decodeWithUUID(content, offset).orElseThrow(() -> new IOException("Could not load from file"));
            nameColorMap.put(triple.getMiddle(), triple.getLeft());
            offset = triple.getRight();
        }
        return nameColorMap;
    }

    @NotNull
    public static UUID bytesToUUID(final byte @NotNull [] bytes) {
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        final long mostSigBits = buffer.getLong();
        final long leastSigBits = buffer.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }

    public static byte @NotNull [] uuidToBytes(@NotNull final UUID uuid) {
        final ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    public String toString() {
        return "MetaNameColor{" +
                "color=" + color +
                ", decoration=" + decoration +
                '}';
    }
}
