package org.crayne.ptjdnc.api.profile;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.crayne.ptjdnc.api.NameStyle;
import org.crayne.ptjdnc.api.color.NameColor;
import org.crayne.ptjdnc.api.io.ContinuousBinaryMap;
import org.crayne.ptjdnc.api.style.NameDecoration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class GlobalProfileFile extends ContinuousBinaryMap<UUID, NameStyle> {

    @NotNull
    private final GlobalNameStyleProfile globalNameStyleProfile;

    public GlobalProfileFile(@NotNull final GlobalNameStyleProfile profile) {
        this.globalNameStyleProfile = profile;
    }

    public byte @NotNull [] [] encode(@NotNull final UUID uuid, @NotNull final NameStyle nameStyle) {
        return new byte[][] {
                uuidToBytes(uuid),
                nameStyle.encode()
        };
    }

    @NotNull
    public Optional<Triple<UUID, NameStyle, Integer>> decode(final byte @NotNull [] encoded, final int offset) {
        if (encoded.length < 16) return Optional.empty();
        final byte[] uuidBytes = Arrays.copyOfRange(encoded, offset, offset + 16);
        final UUID uuid = bytesToUUID(uuidBytes);

        final byte decorationStringEncoded = encoded[offset + 16];

        final Optional<NameDecoration> decoration = NameDecoration.decode(decorationStringEncoded);
        if (decoration.isEmpty()) return Optional.empty();

        final Optional<Pair<NameColor, Integer>> color = NameColor.decode(decoration.get(), encoded, offset + 17);
        if (color.isEmpty()) return Optional.empty();

        final NameStyle nameStyle = new NameStyle(color.get().getLeft(), decoration.get());

        return Optional.of(Triple.of(uuid, nameStyle, color.get().getRight()));
    }

    public void load(@NotNull final Map<UUID, NameStyle> uuidNameStyleMap) throws IOException {
        final File nameColorFile = globalNameStyleProfile.nameColorFile();
        if (!nameColorFile.isFile()) return;

        super.load(nameColorFile, uuidNameStyleMap);
    }

    public void load() throws IOException {
        final Map<UUID, NameStyle> cachedNameStyleMap = globalNameStyleProfile.createNameStyleMap();
        load(cachedNameStyleMap);

        cachedNameStyleMap.forEach(((uuid, nameStyle) -> globalNameStyleProfile.profiles.put(uuid, new NameStyleProfile(uuid, nameStyle))));
    }

    public void save(@NotNull final Map<UUID, NameStyle> uuidNameStyleMap) throws IOException {
        super.save(globalNameStyleProfile.nameColorFile(), uuidNameStyleMap);
    }

    public void save() throws IOException {
        save(globalNameStyleProfile.createNameStyleMap());
    }

}
