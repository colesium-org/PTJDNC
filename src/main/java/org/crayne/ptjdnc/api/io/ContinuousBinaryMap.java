package org.crayne.ptjdnc.api.io;

import com.google.common.primitives.Bytes;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class ContinuousBinaryMap<T, U> {
    
    public ContinuousBinaryMap() {

    }

    public abstract byte @NotNull [] @NotNull [] encode(@NotNull final T t, @NotNull final U u);
    
    public byte @NotNull [] encode1D(@NotNull final T t, @NotNull final U u) {
        return Bytes.concat(encode(t, u));
    }

    @NotNull
    public abstract Optional<Triple<T, U, Integer>> decode(final byte @NotNull [] encoded, final int offset);

    public void save(@NotNull final File file, @NotNull final Map<T, U> tuMap) throws IOException {
        Files.write(file.toPath(), Bytes.concat(tuMap.entrySet()
                .stream()
                .map(e -> encode1D(e.getKey(), e.getValue()))
                .toList()
                .toArray(new byte[0][])));
    }

    public void load(@NotNull final File file, @NotNull final Map<T, U> tuMap) throws IOException {
        final Map<T, U> map = new HashMap<>();

        final byte[] content = Files.readAllBytes(file.toPath());
        int offset = 0;
        while (content.length != offset) {
            final Triple<T, U, Integer> triple = decode(content, offset).orElseThrow(() -> new IOException("Could not load from file"));
            map.put(triple.getLeft(), triple.getMiddle());
            offset = triple.getRight();
        }
        tuMap.putAll(map);
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

}
